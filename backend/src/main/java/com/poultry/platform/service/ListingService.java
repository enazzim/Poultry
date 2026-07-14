package com.poultry.platform.service;

import com.poultry.platform.domain.*;
import com.poultry.platform.dto.ListingDtos;
import com.poultry.platform.repository.*;
import com.poultry.platform.security.SecurityUtils;
import com.poultry.platform.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
public class ListingService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private final ListingRepository listingRepository;
    private final CategoryRepository categoryRepository;
    private final OrganizationRepository organizationRepository;
    private final AppUserRepository appUserRepository;
    private final InterestRepository interestRepository;
    private final InquiryRepository inquiryRepository;
    private final PartnerPreferenceRepository preferenceRepository;
    private final OrganizationCategoryRepository organizationCategoryRepository;
    private final SchemaValidator schemaValidator;
    private final MatchingService matchingService;
    private final NotificationService notificationService;

    public ListingService(ListingRepository listingRepository,
                          CategoryRepository categoryRepository,
                          OrganizationRepository organizationRepository,
                          AppUserRepository appUserRepository,
                          InterestRepository interestRepository,
                          InquiryRepository inquiryRepository,
                          PartnerPreferenceRepository preferenceRepository,
                          OrganizationCategoryRepository organizationCategoryRepository,
                          SchemaValidator schemaValidator,
                          MatchingService matchingService,
                          NotificationService notificationService) {
        this.listingRepository = listingRepository;
        this.categoryRepository = categoryRepository;
        this.organizationRepository = organizationRepository;
        this.appUserRepository = appUserRepository;
        this.interestRepository = interestRepository;
        this.inquiryRepository = inquiryRepository;
        this.preferenceRepository = preferenceRepository;
        this.organizationCategoryRepository = organizationCategoryRepository;
        this.schemaValidator = schemaValidator;
        this.matchingService = matchingService;
        this.notificationService = notificationService;
    }

    @Transactional
    public ListingDtos.ListingResponse create(UserPrincipal principal, ListingDtos.CreateListingRequest request) {
        if (principal.getRole() != UserRole.FARM && principal.getRole() != UserRole.PARTNER && principal.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("공고 등록 권한이 없습니다.");
        }
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리가 없습니다."));
        if (category.getStatus() != CategoryStatus.ACTIVE) {
            throw new IllegalArgumentException("비활성 카테고리에는 공고를 등록할 수 없습니다.");
        }
        schemaValidator.validateSide(category, request.side());
        schemaValidator.validate(category.getId(), request.attributes());

        Organization org = organizationRepository.findById(principal.getOrganizationId())
                .orElseThrow(() -> new IllegalArgumentException("조직이 없습니다."));
        AppUser user = appUserRepository.findById(principal.getId()).orElseThrow();

        Listing listing = new Listing();
        listing.setCategory(category);
        listing.setOrganization(org);
        listing.setCreatedBy(user);
        listing.setSide(request.side());
        listing.setRegionCode(request.regionCode());
        listing.setQuantity(request.quantity());
        listing.setUnit(request.unit());
        listing.setTargetPrice(request.targetPrice());
        listing.setLogisticsType(request.logisticsType());
        listing.setExpiresAt(request.expiresAt());
        listing.setTitle(request.title());
        listing.setMemo(request.memo());
        listing.setAttributes(request.attributes());
        listing.setSource(ListingSource.MANUAL);
        listing.setStatus(ListingStatus.OPEN);
        listingRepository.save(listing);

        matchingService.matchAndNotify(listing);
        return toListingResponse(listing);
    }

    @Transactional
    public List<ListingDtos.ListingResponse> feed(Long categoryId, String regionCode, String side, String listType) {
        expireIfNeeded();
        Instant now = Instant.now();
        String type = listType == null || listType.isBlank() ? "ING" : listType.toUpperCase();
        ListingSide sideEnum = parseSide(side);

        Set<ListingStatus> statuses;
        Instant dayStart = null;
        Instant dayEnd = null;
        switch (type) {
            case "TODAY" -> {
                statuses = EnumSet.of(ListingStatus.OPEN);
                LocalDate today = LocalDate.now(ZONE);
                dayStart = today.atStartOfDay(ZONE).toInstant();
                dayEnd = today.plusDays(1).atStartOfDay(ZONE).toInstant();
            }
            case "CLOSED" -> statuses = EnumSet.of(ListingStatus.CLOSED, ListingStatus.EXPIRED);
            case "ALL" -> statuses = EnumSet.of(ListingStatus.OPEN, ListingStatus.CLOSED, ListingStatus.EXPIRED);
            default -> statuses = EnumSet.of(ListingStatus.OPEN);
        }

        return listingRepository.searchPortal(
                        statuses, categoryId, blankToNull(regionCode), sideEnum, dayStart, dayEnd, now)
                .stream().map(this::toListingResponse).toList();
    }

    @Transactional(readOnly = true)
    public ListingDtos.ListingResponse get(Long id) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공고가 없습니다."));
        touchExpire(listing);
        return toListingResponse(listing);
    }

    @Transactional(readOnly = true)
    public List<ListingDtos.ListingResponse> myListings(Long organizationId) {
        return listingRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId)
                .stream().map(this::toListingResponse).toList();
    }

    @Transactional
    public ListingDtos.ListingResponse close(UserPrincipal principal, Long listingId) {
        Listing listing = getOwnedListing(principal, listingId);
        listing.setStatus(ListingStatus.CLOSED);
        return toListingResponse(listing);
    }

    @Transactional
    public void addInterest(UserPrincipal principal, Long listingId) {
        Listing listing = getOpenListing(listingId);
        ensureNotOwnListing(principal, listing);
        Organization org = organizationRepository.findById(principal.getOrganizationId())
                .orElseThrow(() -> new IllegalArgumentException("조직이 없습니다."));
        ensurePartnerCategoryAccess(org.getId(), listing.getCategory().getId(), principal.getRole());
        if (interestRepository.existsByListingIdAndOrganizationId(listingId, org.getId())) {
            return;
        }
        Interest interest = new Interest();
        interest.setListing(listing);
        interest.setOrganization(org);
        interest.setUser(appUserRepository.findById(principal.getId()).orElse(null));
        interestRepository.save(interest);

        notificationService.notifyOrganization(
                listing.getOrganization().getId(),
                "관심 등록",
                org.getName() + "에서 공고 #" + listing.getId() + "에 관심을 표시했습니다.",
                "INTEREST",
                listing.getId()
        );
    }

    @Transactional
    public ListingDtos.InquiryResponse addInquiry(UserPrincipal principal, Long listingId, ListingDtos.InquiryRequest request) {
        Listing listing = getOpenListing(listingId);
        ensureNotOwnListing(principal, listing);
        Organization org = organizationRepository.findById(principal.getOrganizationId())
                .orElseThrow(() -> new IllegalArgumentException("조직이 없습니다."));
        ensurePartnerCategoryAccess(org.getId(), listing.getCategory().getId(), principal.getRole());

        Inquiry inquiry = new Inquiry();
        inquiry.setListing(listing);
        inquiry.setFromOrganization(org);
        inquiry.setFromUser(appUserRepository.findById(principal.getId()).orElse(null));
        inquiry.setMessage(request.message());
        inquiry.setContactPhone(request.contactPhone() != null ? request.contactPhone() : org.getPhone());
        inquiryRepository.save(inquiry);

        notificationService.notifyOrganization(
                listing.getOrganization().getId(),
                "신규 문의",
                org.getName() + ": " + request.message(),
                "INQUIRY",
                inquiry.getId()
        );
        return toInquiryResponse(inquiry);
    }

    @Transactional(readOnly = true)
    public List<ListingDtos.InquiryResponse> receivedInquiries(Long organizationId) {
        return inquiryRepository.findByListingOrganizationIdOrderByCreatedAtDesc(organizationId)
                .stream().map(this::toInquiryResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ListingDtos.InquiryResponse> sentInquiries(Long organizationId) {
        return inquiryRepository.findByFromOrganizationIdOrderByCreatedAtDesc(organizationId)
                .stream().map(this::toInquiryResponse).toList();
    }

    @Transactional
    public ListingDtos.PreferenceResponse upsertPreference(UserPrincipal principal, ListingDtos.PreferenceRequest request) {
        if (principal.getRole() != UserRole.PARTNER && principal.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("파트너만 선호조건을 설정할 수 있습니다.");
        }
        Organization org = organizationRepository.findById(principal.getOrganizationId())
                .orElseThrow(() -> new IllegalArgumentException("조직이 없습니다."));
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리가 없습니다."));
        ensurePartnerCategoryAccess(org.getId(), category.getId(), principal.getRole());

        PartnerPreference pref = preferenceRepository.findByOrganizationIdAndCategoryId(org.getId(), category.getId())
                .orElseGet(PartnerPreference::new);
        pref.setOrganization(org);
        pref.setCategory(category);
        pref.setRegions(request.regions() != null ? request.regions() : List.of());
        pref.setMinQuantity(request.minQuantity());
        pref.setAttributeFilters(request.attributeFilters());
        pref.setPushEnabled(request.pushEnabled());
        preferenceRepository.save(pref);
        return toPreferenceResponse(pref);
    }

    @Transactional(readOnly = true)
    public List<ListingDtos.PreferenceResponse> myPreferences(Long organizationId) {
        return preferenceRepository.findByOrganizationId(organizationId).stream()
                .map(this::toPreferenceResponse).toList();
    }

    @Transactional
    public ListingDtos.ListingResponse createFromMes(ListingDtos.MesListingRequest request) {
        Category category = categoryRepository.findByCode(request.categoryCode().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("카테고리 코드 없음: " + request.categoryCode()));
        if (category.getStatus() != CategoryStatus.ACTIVE) {
            throw new IllegalArgumentException("비활성 카테고리입니다.");
        }

        var existing = listingRepository.findByFarmCodeAndExternalListingId(request.farmCode(), request.externalListingId());
        if (existing.isPresent()) {
            return toListingResponse(existing.get());
        }

        Organization mapped = organizationRepository.findByFarmCode(request.farmCode())
                .orElseThrow(() -> new IllegalArgumentException(
                        "farmCode에 해당하는 농가가 없습니다(MES 미연동 또는 미등록): " + request.farmCode()));
        if (!mapped.isMesLinked() || mapped.getOrgRole() != UserRole.FARM) {
            throw new IllegalArgumentException("MES 연동 대상 농가가 아닙니다: " + request.farmCode());
        }

        schemaValidator.validateSide(category, request.side());
        schemaValidator.validate(category.getId(), request.attributes());

        Listing listing = new Listing();
        listing.setCategory(category);
        listing.setOrganization(mapped);
        listing.setSide(request.side());
        listing.setRegionCode(request.regionCode());
        listing.setQuantity(request.quantity());
        listing.setUnit(request.unit());
        listing.setTargetPrice(request.targetPrice());
        listing.setLogisticsType(request.logisticsType());
        listing.setExpiresAt(request.expiresAt());
        listing.setTitle(request.title() != null ? request.title() : "MES 출하: " + category.getName());
        listing.setMemo(request.memo());
        listing.setAttributes(request.attributes());
        listing.setSource(ListingSource.MES);
        listing.setFarmCode(request.farmCode());
        listing.setExternalListingId(request.externalListingId());
        listing.setStatus(ListingStatus.OPEN);
        listingRepository.save(listing);
        matchingService.matchAndNotify(listing);
        return toListingResponse(listing);
    }

    @Transactional
    public void applyFeatured(Listing listing, int durationDays) {
        Instant base = listing.getFeaturedUntil() != null && listing.getFeaturedUntil().isAfter(Instant.now())
                ? listing.getFeaturedUntil()
                : Instant.now();
        listing.setFeaturedUntil(base.plusSeconds(durationDays * 24L * 3600L));
        listingRepository.save(listing);
    }

    private void ensurePartnerCategoryAccess(Long orgId, Long categoryId, UserRole role) {
        if (role == UserRole.ADMIN || role == UserRole.FARM) {
            return;
        }
        if (!organizationCategoryRepository.existsByOrganizationIdAndCategoryId(orgId, categoryId)) {
            throw new IllegalArgumentException("해당 카테고리에 대한 권한이 없습니다.");
        }
    }

    private void ensureNotOwnListing(UserPrincipal principal, Listing listing) {
        if (principal.getRole() != UserRole.ADMIN
                && listing.getOrganization().getId().equals(principal.getOrganizationId())) {
            throw new IllegalArgumentException("본인 조직 공고에는 관심/문의할 수 없습니다.");
        }
    }

    private Listing getOpenListing(Long listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("공고가 없습니다."));
        touchExpire(listing);
        if (listing.getStatus() != ListingStatus.OPEN) {
            throw new IllegalArgumentException("열려 있는 공고가 아닙니다.");
        }
        return listing;
    }

    private Listing getOwnedListing(UserPrincipal principal, Long listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("공고가 없습니다."));
        if (principal.getRole() != UserRole.ADMIN
                && !listing.getOrganization().getId().equals(principal.getOrganizationId())) {
            throw new IllegalArgumentException("본인 조직 공고만 마감할 수 있습니다.");
        }
        return listing;
    }

    private void expireIfNeeded() {
        List<Listing> expired = listingRepository.findByStatusAndExpiresAtBefore(ListingStatus.OPEN, Instant.now());
        for (Listing listing : expired) {
            listing.setStatus(ListingStatus.EXPIRED);
        }
    }

    private void touchExpire(Listing listing) {
        if (listing.getExpiresAt() != null
                && listing.getExpiresAt().isBefore(Instant.now())
                && listing.getStatus() == ListingStatus.OPEN) {
            listing.setStatus(ListingStatus.EXPIRED);
        }
    }

    private ListingSide parseSide(String side) {
        if (side == null || side.isBlank()) {
            return null;
        }
        return ListingSide.valueOf(side.toUpperCase());
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private ListingDtos.ListingResponse toListingResponse(Listing listing) {
        String phone = SecurityUtils.currentUserOrNull() != null
                ? listing.getOrganization().getPhone()
                : null;
        Instant featuredUntil = listing.getFeaturedUntil();
        boolean featured = featuredUntil != null && featuredUntil.isAfter(Instant.now());
        return new ListingDtos.ListingResponse(
                listing.getId(),
                listing.getCategory().getId(),
                listing.getCategory().getCode(),
                listing.getCategory().getName(),
                listing.getOrganization().getId(),
                listing.getOrganization().getName(),
                phone,
                listing.getSide(),
                listing.getRegionCode(),
                listing.getQuantity(),
                listing.getUnit(),
                listing.getTargetPrice(),
                listing.getLogisticsType(),
                listing.getExpiresAt(),
                listing.getStatus(),
                listing.getSource(),
                listing.getTitle(),
                listing.getMemo(),
                listing.getAttributes(),
                listing.getCreatedAt(),
                featuredUntil,
                featured
        );
    }

    private ListingDtos.InquiryResponse toInquiryResponse(Inquiry inquiry) {
        return new ListingDtos.InquiryResponse(
                inquiry.getId(),
                inquiry.getListing().getId(),
                inquiry.getListing().getTitle(),
                inquiry.getFromOrganization().getId(),
                inquiry.getFromOrganization().getName(),
                inquiry.getMessage(),
                inquiry.getContactPhone(),
                inquiry.getStatus(),
                inquiry.getReplyMemo(),
                inquiry.getCreatedAt()
        );
    }

    private ListingDtos.PreferenceResponse toPreferenceResponse(PartnerPreference pref) {
        return new ListingDtos.PreferenceResponse(
                pref.getId(),
                pref.getCategory().getId(),
                pref.getCategory().getCode(),
                pref.getCategory().getName(),
                pref.getRegions(),
                pref.getMinQuantity(),
                pref.getAttributeFilters(),
                pref.isPushEnabled()
        );
    }
}
