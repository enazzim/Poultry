package com.poultry.platform.service;

import com.poultry.platform.domain.*;
import com.poultry.platform.dto.AuthDtos;
import com.poultry.platform.repository.*;
import com.poultry.platform.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final OrganizationRepository organizationRepository;
    private final CategoryRepository categoryRepository;
    private final OrganizationCategoryRepository organizationCategoryRepository;
    private final UserInterestRepository userInterestRepository;
    private final PartnerPreferenceRepository preferenceRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthService(AppUserRepository appUserRepository,
                       OrganizationRepository organizationRepository,
                       CategoryRepository categoryRepository,
                       OrganizationCategoryRepository organizationCategoryRepository,
                       UserInterestRepository userInterestRepository,
                       PartnerPreferenceRepository preferenceRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       AuthenticationManager authenticationManager) {
        this.appUserRepository = appUserRepository;
        this.organizationRepository = organizationRepository;
        this.categoryRepository = categoryRepository;
        this.organizationCategoryRepository = organizationCategoryRepository;
        this.userInterestRepository = userInterestRepository;
        this.preferenceRepository = preferenceRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        if (request.role() == UserRole.ADMIN) {
            throw new IllegalArgumentException("관리자 계정은 시드로만 생성됩니다.");
        }
        if (appUserRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        List<String> interests = request.interestCategoryCodes() != null ? request.interestCategoryCodes() : List.of();
        if (interests.isEmpty()) {
            throw new IllegalArgumentException("관심 분야를 1개 이상 선택해야 합니다.");
        }
        if (!request.smsConsent() && !request.alimtalkConsent()) {
            throw new IllegalArgumentException("SMS 또는 알림톡 수신 동의가 필요합니다.");
        }
        if (request.phone() == null || request.phone().isBlank()) {
            throw new IllegalArgumentException("문자/알림톡 수신을 위해 연락처(휴대폰)가 필요합니다.");
        }

        Organization org = new Organization();
        org.setName(request.organizationName());
        org.setOrgRole(request.role());
        org.setRegionCode(request.regionCode());
        org.setPhone(request.phone());
        if (request.role() == UserRole.FARM && request.farmCode() != null && !request.farmCode().isBlank()) {
            String code = request.farmCode().trim();
            if (organizationRepository.findByFarmCode(code).isPresent()) {
                throw new IllegalArgumentException("이미 사용 중인 MES farmCode입니다: " + code);
            }
            org.setFarmCode(code);
        }
        org.setApproved(true);
        organizationRepository.save(org);

        if (request.role() == UserRole.PARTNER) {
            List<String> codes = request.categoryCodes() != null ? request.categoryCodes() : List.of();
            if (codes.isEmpty()) {
                throw new IllegalArgumentException("파트너는 취급 카테고리를 1개 이상 선택해야 합니다.");
            }
            for (String code : codes) {
                Category category = categoryRepository.findByCode(code)
                        .orElseThrow(() -> new IllegalArgumentException("카테고리 없음: " + code));
                OrganizationCategory oc = new OrganizationCategory();
                oc.setOrganization(org);
                oc.setCategory(category);
                organizationCategoryRepository.save(oc);

                PartnerPreference pref = preferenceRepository
                        .findByOrganizationIdAndCategoryId(org.getId(), category.getId())
                        .orElseGet(PartnerPreference::new);
                pref.setOrganization(org);
                pref.setCategory(category);
                pref.setRegions(request.regionCode() != null && !request.regionCode().isBlank()
                        ? List.of(request.regionCode()) : List.of());
                pref.setPushEnabled(true);
                preferenceRepository.save(pref);
            }
        }

        AppUser user = new AppUser();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName());
        user.setRole(request.role());
        user.setOrganization(org);
        user.setNotifyPhone(request.phone().trim());
        user.setSmsConsent(request.smsConsent());
        user.setAlimtalkConsent(request.alimtalkConsent());
        user.setConsentAt(Instant.now());
        appUserRepository.save(user);

        for (String code : interests) {
            Category category = categoryRepository.findByCode(code)
                    .orElseThrow(() -> new IllegalArgumentException("관심 카테고리 없음: " + code));
            UserInterest interest = new UserInterest();
            interest.setUser(user);
            interest.setCategory(category);
            userInterestRepository.save(interest);
        }

        return toAuthResponse(user, org);
    }

    @Transactional(readOnly = true)
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        AppUser user = appUserRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return toAuthResponse(user, user.getOrganization());
    }

    private AuthDtos.AuthResponse toAuthResponse(AppUser user, Organization org) {
        Long orgId = org != null ? org.getId() : null;
        String orgName = org != null ? org.getName() : null;
        String farmCode = org != null && org.isMesLinked() ? org.getFarmCode() : null;
        String token = jwtTokenProvider.createToken(user.getId(), user.getUsername(), user.getRole(), orgId);
        return new AuthDtos.AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getRole(),
                orgId,
                orgName,
                farmCode,
                farmCode != null,
                user.isSmsConsent(),
                user.isAlimtalkConsent());
    }
}
