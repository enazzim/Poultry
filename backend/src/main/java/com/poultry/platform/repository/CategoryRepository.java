package com.poultry.platform.repository;

import com.poultry.platform.domain.Category;
import com.poultry.platform.domain.CategoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByCode(String code);
    boolean existsByCode(String code);
    List<Category> findByStatusOrderBySortOrderAsc(CategoryStatus status);
    List<Category> findAllByOrderBySortOrderAsc();
}
