package com.poultry.platform.service;

import com.poultry.platform.domain.*;
import com.poultry.platform.repository.PartnerPreferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class MatchingService {

    private final PartnerPreferenceRepository preferenceRepository;
    private final NotificationService notificationService;
    private final CategoryAttributeDefRepositoryProxy attributeProxy;

    public MatchingService(PartnerPreferenceRepository preferenceRepository,
                           NotificationService notificationService,
                           CategoryAttributeDefRepositoryProxy attributeProxy) {
        this.preferenceRepository = preferenceRepository;
        this.notificationService = notificationService;
        this.attributeProxy = attributeProxy;
    }

    @Transactional
    public void matchAndNotify(Listing listing) {
        if (listing.getStatus() != ListingStatus.OPEN) {
            return;
        }
        List<PartnerPreference> prefs = preferenceRepository.findByCategoryIdAndPushEnabledTrue(listing.getCategory().getId());
        String highlight = attributeProxy.buildNotifySnippet(listing);

        for (PartnerPreference pref : prefs) {
            if (pref.getOrganization().getId().equals(listing.getOrganization().getId())) {
                continue;
            }
            if (!matchesRegion(pref, listing)) {
                continue;
            }
            if (pref.getMinQuantity() != null && listing.getQuantity().compareTo(pref.getMinQuantity()) < 0) {
                continue;
            }
            if (!matchesAttributes(pref.getAttributeFilters(), listing.getAttributes())) {
                continue;
            }
            String title = "[" + listing.getCategory().getName() + "] 신규 공고";
            String body = (listing.getTitle() != null ? listing.getTitle() + " · " : "")
                    + listing.getQuantity() + listing.getUnit()
                    + " · " + listing.getRegionCode()
                    + (highlight.isBlank() ? "" : " · " + highlight);
            notificationService.notifyOrganization(
                    pref.getOrganization().getId(),
                    title,
                    body,
                    "LISTING_MATCH",
                    listing.getId()
            );
        }
    }

    private boolean matchesRegion(PartnerPreference pref, Listing listing) {
        if (pref.getRegions() == null || pref.getRegions().isEmpty()) {
            return true;
        }
        return pref.getRegions().contains(listing.getRegionCode());
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

    /** Thin wrapper to avoid circular deps / keep MatchingService focused */
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
