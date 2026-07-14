package com.poultry.platform.repository;

import com.poultry.platform.domain.OrganizationCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrganizationCategoryRepository extends JpaRepository<OrganizationCategory, Long> {
    List<OrganizationCategory> findByOrganizationId(Long organizationId);
    boolean existsByOrganizationIdAndCategoryId(Long organizationId, Long categoryId);
}
