package com.poultry.platform.repository;

import com.poultry.platform.domain.ProductOrder;
import com.poultry.platform.domain.ProductOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductOrderRepository extends JpaRepository<ProductOrder, Long> {
    List<ProductOrder> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);
    List<ProductOrder> findByStatusOrderByCreatedAtDesc(ProductOrderStatus status);
    List<ProductOrder> findAllByOrderByCreatedAtDesc();
}
