package com.poultry.platform.repository;

import com.poultry.platform.domain.PartnerPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PartnerPreferenceRepository extends JpaRepository<PartnerPreference, Long> {
    List<PartnerPreference> findByCategoryIdAndPushEnabledTrue(Long categoryId);
    List<PartnerPreference> findByOrganizationId(Long organizationId);
    Optional<PartnerPreference> findByOrganizationIdAndCategoryId(Long organizationId, Long categoryId);
}
