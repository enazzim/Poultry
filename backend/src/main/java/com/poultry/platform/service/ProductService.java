package com.poultry.platform.service;

import com.poultry.platform.domain.*;
import com.poultry.platform.dto.ProductDtos;
import com.poultry.platform.repository.*;
import com.poultry.platform.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Service
public class ProductService {

    private final PortalProductRepository productRepository;
    private final ProductOrderRepository orderRepository;
    private final OrganizationRepository organizationRepository;
    private final ListingRepository listingRepository;
    private final ListingService listingService;

    public ProductService(PortalProductRepository productRepository,
                          ProductOrderRepository orderRepository,
                          OrganizationRepository organizationRepository,
                          ListingRepository listingRepository,
                          ListingService listingService) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.organizationRepository = organizationRepository;
        this.listingRepository = listingRepository;
        this.listingService = listingService;
    }

    @Transactional(readOnly = true)
    public List<ProductDtos.ProductResponse> activeProducts() {
        return productRepository.findByActiveTrueOrderByIdAsc().stream().map(this::toProduct).toList();
    }

    @Transactional
    public ProductDtos.OrderResponse placeOrder(UserPrincipal principal, ProductDtos.OrderRequest request) {
        PortalProduct product = productRepository.findById(request.productId())
                .orElseThrow(() -> new IllegalArgumentException("상품이 없습니다."));
        if (!product.isActive()) {
            throw new IllegalArgumentException("비활성 상품입니다.");
        }
        Organization org = organizationRepository.findById(principal.getOrganizationId())
                .orElseThrow(() -> new IllegalArgumentException("조직이 없습니다."));

        Listing listing = null;
        if (request.listingId() != null) {
            listing = listingRepository.findById(request.listingId())
                    .orElseThrow(() -> new IllegalArgumentException("공고가 없습니다."));
            if (principal.getRole() != UserRole.ADMIN
                    && !listing.getOrganization().getId().equals(org.getId())) {
                throw new IllegalArgumentException("본인 조직 공고만 상품에 연결할 수 있습니다.");
            }
        } else if ("FEATURED_LISTING".equals(product.getCode())) {
            throw new IllegalArgumentException("추천 노출 상품은 공고를 선택해야 합니다.");
        }

        ProductOrder order = new ProductOrder();
        order.setOrganization(org);
        order.setProduct(product);
        order.setListing(listing);
        order.setMemo(request.memo());
        order.setStatus(ProductOrderStatus.PENDING);
        orderRepository.save(order);
        return toOrder(order);
    }

    @Transactional(readOnly = true)
    public List<ProductDtos.OrderResponse> myOrders(Long organizationId) {
        return orderRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId)
                .stream().map(this::toOrder).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductDtos.OrderResponse> adminOrders(String status) {
        if (status != null && !status.isBlank()) {
            ProductOrderStatus st = ProductOrderStatus.valueOf(status.toUpperCase());
            return orderRepository.findByStatusOrderByCreatedAtDesc(st).stream().map(this::toOrder).toList();
        }
        return orderRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toOrder).toList();
    }

    @Transactional
    public ProductDtos.OrderResponse approve(Long orderId, ProductDtos.OrderDecisionRequest request) {
        ProductOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("신청이 없습니다."));
        if (order.getStatus() != ProductOrderStatus.PENDING) {
            throw new IllegalArgumentException("대기 중인 신청만 승인할 수 있습니다.");
        }
        order.setStatus(ProductOrderStatus.APPROVED);
        order.setAdminMemo(request != null ? request.adminMemo() : null);
        order.setDecidedAt(Instant.now());

        if (order.getListing() != null && "FEATURED_LISTING".equals(order.getProduct().getCode())) {
            listingService.applyFeatured(order.getListing(), order.getProduct().getDurationDays());
        }
        return toOrder(order);
    }

    @Transactional
    public ProductDtos.OrderResponse reject(Long orderId, ProductDtos.OrderDecisionRequest request) {
        ProductOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("신청이 없습니다."));
        if (order.getStatus() != ProductOrderStatus.PENDING) {
            throw new IllegalArgumentException("대기 중인 신청만 거절할 수 있습니다.");
        }
        order.setStatus(ProductOrderStatus.REJECTED);
        order.setAdminMemo(request != null ? request.adminMemo() : null);
        order.setDecidedAt(Instant.now());
        return toOrder(order);
    }

    private ProductDtos.ProductResponse toProduct(PortalProduct product) {
        return new ProductDtos.ProductResponse(
                product.getId(),
                product.getCode(),
                product.getName(),
                product.getDescription(),
                product.getPriceHint(),
                product.getDurationDays(),
                product.isActive()
        );
    }

    private ProductDtos.OrderResponse toOrder(ProductOrder order) {
        return new ProductDtos.OrderResponse(
                order.getId(),
                order.getProduct().getId(),
                order.getProduct().getCode(),
                order.getProduct().getName(),
                order.getOrganization().getId(),
                order.getOrganization().getName(),
                order.getListing() != null ? order.getListing().getId() : null,
                order.getListing() != null ? order.getListing().getTitle() : null,
                order.getStatus(),
                order.getMemo(),
                order.getAdminMemo(),
                order.getCreatedAt(),
                order.getDecidedAt()
        );
    }
}
