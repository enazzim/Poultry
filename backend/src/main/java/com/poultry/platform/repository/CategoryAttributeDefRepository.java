package com.poultry.platform.repository;

import com.poultry.platform.domain.CategoryAttributeDef;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoryAttributeDefRepository extends JpaRepository<CategoryAttributeDef, Long> {
    List<CategoryAttributeDef> findByCategoryIdOrderBySortOrderAsc(Long categoryId);
    Optional<CategoryAttributeDef> findByIdAndCategoryId(Long id, Long categoryId);
    void deleteByCategoryId(Long categoryId);
}
