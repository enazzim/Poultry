package com.poultry.platform.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poultry.platform.domain.*;
import com.poultry.platform.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Configuration
public class SeedDataConfig {

    @Bean
    CommandLineRunner seedData(CategoryRepository categoryRepository,
                               CategoryAttributeDefRepository attributeDefRepository,
                               OrganizationRepository organizationRepository,
                               OrganizationCategoryRepository organizationCategoryRepository,
                               AppUserRepository appUserRepository,
                               PartnerPreferenceRepository preferenceRepository,
                               PortalArticleRepository articleRepository,
                               PortalProductRepository productRepository,
                               ListingRepository listingRepository,
                               UserInterestRepository userInterestRepository,
                               PasswordEncoder passwordEncoder,
                               ObjectMapper objectMapper) {
        return args -> {
            if (categoryRepository.count() == 0) {
                try (InputStream in = new ClassPathResource("seed-categories.json").getInputStream()) {
                    String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                    List<Map<String, Object>> categories = objectMapper.readValue(json, new TypeReference<>() {});
                    for (Map<String, Object> catMap : categories) {
                        Category category = new Category();
                        category.setCode((String) catMap.get("code"));
                        category.setName((String) catMap.get("name"));
                        category.setDescription((String) catMap.get("description"));
                        category.setAllowedSides(AllowedSides.valueOf((String) catMap.get("allowedSides")));
                        category.setDefaultUnit((String) catMap.get("defaultUnit"));
                        category.setSortOrder(((Number) catMap.get("sortOrder")).intValue());
                        category.setStatus(CategoryStatus.ACTIVE);
                        categoryRepository.save(category);

                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> attrs = (List<Map<String, Object>>) catMap.get("attributes");
                        for (Map<String, Object> a : attrs) {
                            CategoryAttributeDef def = new CategoryAttributeDef();
                            def.setCategory(category);
                            def.setFieldKey((String) a.get("fieldKey"));
                            def.setLabel((String) a.get("label"));
                            def.setDataType(AttributeDataType.valueOf((String) a.get("dataType")));
                            def.setRequired(Boolean.TRUE.equals(a.get("required")));
                            @SuppressWarnings("unchecked")
                            List<String> enumOptions = (List<String>) a.getOrDefault("enumOptions", List.of());
                            def.setEnumOptions(enumOptions != null ? enumOptions : List.of());
                            def.setSortOrder(((Number) a.get("sortOrder")).intValue());
                            def.setMatchable(Boolean.TRUE.equals(a.get("matchable")));
                            def.setShowInList(true);
                            def.setShowInNotify(Boolean.TRUE.equals(a.get("showInNotify")));
                            attributeDefRepository.save(def);
                        }
                    }
                }
            }

            if (!appUserRepository.existsByUsername("admin")) {
                Organization adminOrg = new Organization();
                adminOrg.setName("플랫폼운영");
                adminOrg.setOrgRole(UserRole.ADMIN);
                adminOrg.setPhone("000-0000-0000");
                organizationRepository.save(adminOrg);

                AppUser admin = new AppUser();
                admin.setUsername("admin");
                admin.setPasswordHash(passwordEncoder.encode("admin1234"));
                admin.setDisplayName("운영자");
                admin.setRole(UserRole.ADMIN);
                admin.setOrganization(adminOrg);
                appUserRepository.save(admin);
            }

            if (!appUserRepository.existsByUsername("farm1")) {
                Organization farm = new Organization();
                farm.setName("경산A농장");
                farm.setOrgRole(UserRole.FARM);
                farm.setRegionCode("경북");
                farm.setPhone("010-1111-1111");
                farm.setFarmCode("FARM-GB-001");
                organizationRepository.save(farm);

                AppUser farmUser = new AppUser();
                farmUser.setUsername("farm1");
                farmUser.setPasswordHash(passwordEncoder.encode("farm1234"));
                farmUser.setDisplayName("농장주");
                farmUser.setRole(UserRole.FARM);
                farmUser.setOrganization(farm);
                farmUser.setNotifyPhone("010-1111-1111");
                farmUser.setSmsConsent(true);
                farmUser.setAlimtalkConsent(true);
                farmUser.setConsentAt(Instant.now());
                appUserRepository.save(farmUser);
                categoryRepository.findByCode("EGG").ifPresent(egg -> {
                    UserInterest ui = new UserInterest();
                    ui.setUser(farmUser);
                    ui.setCategory(egg);
                    userInterestRepository.save(ui);
                });
            }

            if (!appUserRepository.existsByUsername("farm2")) {
                Organization farmManual = new Organization();
                farmManual.setName("영천B농장");
                farmManual.setOrgRole(UserRole.FARM);
                farmManual.setRegionCode("경북");
                farmManual.setPhone("010-3333-3333");
                organizationRepository.save(farmManual);

                AppUser farm2User = new AppUser();
                farm2User.setUsername("farm2");
                farm2User.setPasswordHash(passwordEncoder.encode("farm1234"));
                farm2User.setDisplayName("농장주(수동)");
                farm2User.setRole(UserRole.FARM);
                farm2User.setOrganization(farmManual);
                farm2User.setNotifyPhone("010-3333-3333");
                farm2User.setSmsConsent(true);
                farm2User.setAlimtalkConsent(false);
                farm2User.setConsentAt(Instant.now());
                appUserRepository.save(farm2User);
                categoryRepository.findByCode("FEED").ifPresent(feed -> {
                    UserInterest ui = new UserInterest();
                    ui.setUser(farm2User);
                    ui.setCategory(feed);
                    userInterestRepository.save(ui);
                });
            }

            if (!appUserRepository.existsByUsername("dealer1")) {
                Organization dealer = new Organization();
                dealer.setName("대구계란도매");
                dealer.setOrgRole(UserRole.PARTNER);
                dealer.setRegionCode("대구");
                dealer.setPhone("010-2222-2222");
                organizationRepository.save(dealer);

                categoryRepository.findByCode("EGG").ifPresent(egg -> {
                    OrganizationCategory oc = new OrganizationCategory();
                    oc.setOrganization(dealer);
                    oc.setCategory(egg);
                    organizationCategoryRepository.save(oc);

                    PartnerPreference pref = new PartnerPreference();
                    pref.setOrganization(dealer);
                    pref.setCategory(egg);
                    pref.setRegions(List.of("경북", "대구"));
                    pref.setPushEnabled(true);
                    preferenceRepository.save(pref);
                });

                AppUser dealerUser = new AppUser();
                dealerUser.setUsername("dealer1");
                dealerUser.setPasswordHash(passwordEncoder.encode("dealer1234"));
                dealerUser.setDisplayName("도매담당");
                dealerUser.setRole(UserRole.PARTNER);
                dealerUser.setOrganization(dealer);
                dealerUser.setNotifyPhone("010-2222-2222");
                dealerUser.setSmsConsent(true);
                dealerUser.setAlimtalkConsent(true);
                dealerUser.setConsentAt(Instant.now());
                appUserRepository.save(dealerUser);
                categoryRepository.findByCode("EGG").ifPresent(egg -> {
                    UserInterest ui = new UserInterest();
                    ui.setUser(dealerUser);
                    ui.setCategory(egg);
                    userInterestRepository.save(ui);
                });
            }

            // Backfill consent/interests for existing seed users (avoid lazy org access)
            Map<String, String> seedPhones = Map.of(
                    "dealer1", "010-2222-2222",
                    "farm1", "010-1111-1111",
                    "farm2", "010-3333-3333"
            );
            for (Map.Entry<String, String> entry : seedPhones.entrySet()) {
                appUserRepository.findByUsername(entry.getKey()).ifPresent(u -> {
                    if (!u.isSmsConsent() && !u.isAlimtalkConsent()) {
                        u.setNotifyPhone(entry.getValue());
                        u.setSmsConsent(true);
                        u.setAlimtalkConsent(true);
                        u.setConsentAt(Instant.now());
                        appUserRepository.save(u);
                    }
                    if (userInterestRepository.findByUserId(u.getId()).isEmpty()) {
                        categoryRepository.findByCode("EGG").ifPresent(egg -> {
                            UserInterest ui = new UserInterest();
                            ui.setUser(u);
                            ui.setCategory(egg);
                            userInterestRepository.save(ui);
                        });
                    }
                });
            }

            if (!productRepository.existsByCode("FEATURED_LISTING")) {
                PortalProduct featured = new PortalProduct();
                featured.setCode("FEATURED_LISTING");
                featured.setName("공고 추천 노출");
                featured.setDescription("목록·홈 상단에 공고를 강조 노출합니다. (신청 후 운영자 승인)");
                featured.setPriceHint(new BigDecimal("50000"));
                featured.setDurationDays(7);
                featured.setActive(true);
                productRepository.save(featured);
            }
            if (!productRepository.existsByCode("HOME_BANNER")) {
                PortalProduct banner = new PortalProduct();
                banner.setCode("HOME_BANNER");
                banner.setName("홈 배너 안내");
                banner.setDescription("포털 홈 상품 영역에 기관명을 노출합니다. (신청 후 운영자 승인, PG 미연동)");
                banner.setPriceHint(new BigDecimal("100000"));
                banner.setDurationDays(14);
                banner.setActive(true);
                productRepository.save(banner);
            }

            if (articleRepository.count() == 0) {
                AppUser admin = appUserRepository.findByUsername("admin").orElse(null);
                PortalArticle news = new PortalArticle();
                news.setType(ArticleType.NEWS);
                news.setTitle("양계 수급 플랫폼 PoultryShare 오픈");
                news.setSummary("농가와 파트너가 공개 포털에서 수급 공고를 찾고 연결합니다.");
                news.setBody("PoultryShare는 계란·병아리·계분·사료·백신 등 양계 수급 정보를 공개 탐색하는 포털입니다.\n관심·문의로 연결하고, 계약은 오프라인으로 진행합니다.");
                news.setPublished(true);
                news.setPublishedAt(Instant.now());
                news.setCreatedBy(admin);
                articleRepository.save(news);

                PortalArticle notice = new PortalArticle();
                notice.setType(ArticleType.NOTICE);
                notice.setTitle("서비스 이용 안내");
                notice.setSummary("비회원도 공고·뉴스를 열람할 수 있습니다.");
                notice.setBody("로그인 없이 공고와 업계 소식을 둘러볼 수 있습니다.\n관심·문의·공고 등록·유료 노출 신청은 회원 로그인 후 이용하세요.");
                notice.setPublished(true);
                notice.setPublishedAt(Instant.now());
                notice.setCreatedBy(admin);
                articleRepository.save(notice);
            }

            if (listingRepository.count() == 0) {
                categoryRepository.findByCode("EGG").ifPresent(egg -> {
                    organizationRepository.findAll().stream()
                            .filter(o -> o.getOrgRole() == UserRole.FARM && "경산A농장".equals(o.getName()))
                            .findFirst()
                            .ifPresent(farm -> {
                                Listing offer = new Listing();
                                offer.setCategory(egg);
                                offer.setOrganization(farm);
                                offer.setSide(ListingSide.OFFER);
                                offer.setRegionCode("경북");
                                offer.setQuantity(new BigDecimal("80"));
                                offer.setUnit("TRAY");
                                offer.setTargetPrice(new BigDecimal("4800"));
                                offer.setLogisticsType(LogisticsType.PICKUP);
                                offer.setExpiresAt(Instant.now().plus(2, ChronoUnit.DAYS));
                                offer.setTitle("잉여 대란 출하");
                                offer.setMemo("당일 선별분");
                                offer.setAttributes(Map.of("eggGrade", "대란", "eggType", "일반"));
                                offer.setSource(ListingSource.MANUAL);
                                offer.setStatus(ListingStatus.OPEN);
                                offer.setFeaturedUntil(Instant.now().plus(5, ChronoUnit.DAYS));
                                listingRepository.save(offer);
                            });

                    organizationRepository.findAll().stream()
                            .filter(o -> o.getOrgRole() == UserRole.PARTNER)
                            .findFirst()
                            .ifPresent(dealer -> {
                                Listing need = new Listing();
                                need.setCategory(egg);
                                need.setOrganization(dealer);
                                need.setSide(ListingSide.NEED);
                                need.setRegionCode("대구");
                                need.setQuantity(new BigDecimal("100"));
                                need.setUnit("TRAY");
                                need.setTargetPrice(new BigDecimal("4500"));
                                need.setLogisticsType(LogisticsType.DELIVERY);
                                need.setExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS));
                                need.setTitle("중란 매입 희망");
                                need.setAttributes(Map.of("eggGrade", "중란", "eggType", "일반"));
                                need.setSource(ListingSource.MANUAL);
                                need.setStatus(ListingStatus.OPEN);
                                listingRepository.save(need);
                            });
                });
            }
        };
    }
}
