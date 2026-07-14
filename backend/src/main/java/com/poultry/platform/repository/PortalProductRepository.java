package com.poultry.platform.repository;

import com.poultry.platform.domain.PortalProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PortalProductRepository extends JpaRepository<PortalProduct, Long> {
    List<PortalProduct> findByActiveTrueOrderByIdAsc();
    Optional<PortalProduct> findByCode(String code);
    boolean existsByCode(String code);
}
