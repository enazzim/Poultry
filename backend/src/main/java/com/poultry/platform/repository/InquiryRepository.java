package com.poultry.platform.repository;

import com.poultry.platform.domain.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    List<Inquiry> findByListingOrganizationIdOrderByCreatedAtDesc(Long organizationId);
    List<Inquiry> findByFromOrganizationIdOrderByCreatedAtDesc(Long organizationId);
    List<Inquiry> findByListingIdOrderByCreatedAtDesc(Long listingId);
}
