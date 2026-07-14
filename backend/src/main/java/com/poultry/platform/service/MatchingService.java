package com.poultry.platform.service;

import com.poultry.platform.domain.*;
import com.poultry.platform.notify.*;
import com.poultry.platform.repository.AppUserRepository;
import com.poultry.platform.repository.PartnerPreferenceRepository;
import com.poultry.platform.repository.UserInterestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class MatchingService {

    private final PartnerPreferenceRepository preferenceRepository;
    private final UserInterestRepository userInterestRepository;
    private final AppUserRepository appUserRepository;
    private final NotifyPort notifyPort;
    private final CategoryAttributeDefRepositoryProxy attributeProxy;

    public MatchingService(PartnerPreferenceRepository preferenceRepository,
                           UserInterestRepository userInterestRepository,
                           AppUserRepository appUserRepository,
                           NotifyPort notifyPort,
                           CategoryAttributeDefRepositoryProxy attributeProxy) {
        this.preferenceRepository = preferenceRepository;
        this.userInterestRepository = userInterestRepository;
        this.appUserRepository = appUserRepository;
        this.notifyPort = notifyPort;
        this.attributeProxy = attributeProxy;
    }

    @Transactional
    public void matchAndNotify(Listing listing) {
        if (listing.getStatus() != ListingStatus.OPEN) {
            return;
        }
        String highlight = attributeProxy.buildNotifySnippet(listing);
        String title = "[" + listing.getCategory().getName() + "] 관심분야 신규 공고";
        String body = (listing.getTitle() != null ? listing.getTitle() + " · " : "")
                + listing.getQuantity() + listing.getUnit()
                + " · " + listing.getRegionCode()
                + (highlight.isBlank() ? "" : " · " + highlight);

        Map<String, String> templateVars = Map.of(
                "#{category}", listing.getCategory().getName(),
                "#{title}", listing.getTitle() != null ? listing.getTitle() : ("공고#" + listing.getId()),
                "#{region}", listing.getRegionCode() != null ? listing.getRegionCode() : ""
        );

        Set<Long> notifiedUserIds = new HashSet<>();

        for (UserInterest interest : userInterestRepository.findByCategoryIdFetchUser(listing.getCategory().getId())) {
            AppUser user = interest.getUser();
            if (sameOrg(user, listing)) {
                continue;
            }
            if (notifiedUserIds.add(user.getId())) {
                dispatchToUser(user, title, body, listing.getId(), templateVars);
            }
        }

        List<PartnerPreference> prefs = preferenceRepository.findByCategoryIdAndPushEnabledTrue(listing.getCategory().getId());
        for (PartnerPreference pref : prefs) {
            if (pref.getOrganization().getId().equals(listing.getOrganization().getId())) {
                continue;
            }
            if (!matchesRegion(pref, listing) || !matchesQuantity(pref, listing)
                    || !matchesAttributes(pref.getAttributeFilters(), listing.getAttributes())) {
                continue;
            }
            for (AppUser user : appUserRepository.findByOrganizationId(pref.getOrganization().getId())) {
                if (notifiedUserIds.add(user.getId())) {
                    dispatchToUser(user, title, body, listing.getId(), templateVars);
                }
            }
        }
    }

    private boolean sameOrg(AppUser user, Listing listing) {
        return user.getOrganization() != null
                && user.getOrganization().getId().equals(listing.getOrganization().getId());
    }

    private void dispatchToUser(AppUser user, String title, String body, Long listingId,
                                Map<String, String> templateVars) {
        String phone = user.getNotifyPhone();
        if ((phone == null || phone.isBlank()) && user.getOrganization() != null) {
            phone = user.getOrganization().getPhone();
        }
        NotifyRecipient recipient = new NotifyRecipient(
                user.getId(),
                user.getDisplayName(),
                phone,
                user.isSmsConsent(),
                user.isAlimtalkConsent(),
                true
        );
        NotifyMessage message = new NotifyMessage(title, body, "LISTING_MATCH", listingId, templateVars);
        notifyPort.dispatch(NotifyRequest.interestMatch(
                recipient, message, user.isSmsConsent(), user.isAlimtalkConsent()));
    }

    private boolean matchesRegion(PartnerPreference pref, Listing listing) {
        if (pref.getRegions() == null || pref.getRegions().isEmpty()) {
            return true;
        }
        return pref.getRegions().contains(listing.getRegionCode());
    }

    private boolean matchesQuantity(PartnerPreference pref, Listing listing) {
        return pref.getMinQuantity() == null || listing.getQuantity().compareTo(pref.getMinQuantity()) >= 0;
    }

    private boolean matchesAttributes(Map<String, Object> filters, Map<String, Object> attrs) {
        if (filters == null || filters.isEmpty()) {
            return true;
        }
        Map<String, Object> attributes = attrs != null ? attrs : Map.of();
        for (Map.Entry<String, Object> entry : filters.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            Object actual = attributes.get(entry.getKey());
            if (!Objects.equals(String.valueOf(entry.getValue()), String.valueOf(actual))) {
                return false;
            }
        }
        return true;
    }

    @Service
    public static class CategoryAttributeDefRepositoryProxy {
        private final com.poultry.platform.repository.CategoryAttributeDefRepository repo;

        public CategoryAttributeDefRepositoryProxy(com.poultry.platform.repository.CategoryAttributeDefRepository repo) {
            this.repo = repo;
        }

        public String buildNotifySnippet(Listing listing) {
            List<CategoryAttributeDef> defs = repo.findByCategoryIdOrderBySortOrderAsc(listing.getCategory().getId());
            StringBuilder sb = new StringBuilder();
            Map<String, Object> attrs = listing.getAttributes() != null ? listing.getAttributes() : Map.of();
            for (CategoryAttributeDef def : defs) {
                if (!def.isShowInNotify()) {
                    continue;
                }
                Object val = attrs.get(def.getFieldKey());
                if (val == null) {
                    continue;
                }
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(def.getLabel()).append("=").append(val);
            }
            return sb.toString();
        }
    }
}
