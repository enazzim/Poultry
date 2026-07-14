package com.poultry.platform.repository;

import com.poultry.platform.domain.Listing;
import com.poultry.platform.domain.ListingSide;
import com.poultry.platform.domain.ListingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ListingRepository extends JpaRepository<Listing, Long> {
    List<Listing> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);

    @Query("""
        select l from Listing l
        where l.status in :statuses
          and (:categoryId is null or l.category.id = :categoryId)
          and (:regionCode is null or l.regionCode = :regionCode)
          and (:side is null or l.side = :side)
          and (
            :dayStart is null
            or (l.expiresAt is not null and l.expiresAt >= :dayStart and l.expiresAt < :dayEnd)
          )
        order by
          case when l.featuredUntil is not null and l.featuredUntil > :now then 0 else 1 end,
          l.featuredUntil desc,
          l.createdAt desc
        """)
    List<Listing> searchPortal(@Param("statuses") Collection<ListingStatus> statuses,
                               @Param("categoryId") Long categoryId,
                               @Param("regionCode") String regionCode,
                               @Param("side") ListingSide side,
                               @Param("dayStart") Instant dayStart,
                               @Param("dayEnd") Instant dayEnd,
                               @Param("now") Instant now);

    List<Listing> findByStatusAndExpiresAtBefore(ListingStatus status, Instant before);

    Optional<Listing> findByFarmCodeAndExternalListingId(String farmCode, String externalListingId);
}
