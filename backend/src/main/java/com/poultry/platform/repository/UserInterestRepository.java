package com.poultry.platform.repository;

import com.poultry.platform.domain.UserInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {
    List<UserInterest> findByUserId(Long userId);

    @Query("""
        select ui from UserInterest ui
        join fetch ui.user u
        where ui.category.id = :categoryId
        """)
    List<UserInterest> findByCategoryIdFetchUser(@Param("categoryId") Long categoryId);
}
