package com.poultry.platform.repository;

import com.poultry.platform.domain.Interest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InterestRepository extends JpaRepository<Interest, Long> {
    boolean existsByListingIdAndOrganizationId(Long listingId, Long organizationId);
    List<Interest> findByListingIdOrderByCreatedAtDesc(Long listingId);
    List<Interest> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);
}
