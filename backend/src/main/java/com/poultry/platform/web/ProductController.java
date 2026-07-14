package com.poultry.platform.web;

import com.poultry.platform.dto.ProductDtos;
import com.poultry.platform.security.SecurityUtils;
import com.poultry.platform.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public List<ProductDtos.ProductResponse> products() {
        return productService.activeProducts();
    }

    @PostMapping("/products/orders")
    public ProductDtos.OrderResponse place(@Valid @RequestBody ProductDtos.OrderRequest request) {
        return productService.placeOrder(SecurityUtils.currentUser(), request);
    }

    @GetMapping("/products/orders/me")
    public List<ProductDtos.OrderResponse> myOrders() {
        return productService.myOrders(SecurityUtils.currentUser().getOrganizationId());
    }

    @GetMapping("/admin/product-orders")
    public List<ProductDtos.OrderResponse> adminOrders(@RequestParam(required = false) String status) {
        return productService.adminOrders(status);
    }

    @PostMapping("/admin/product-orders/{id}/approve")
    public ProductDtos.OrderResponse approve(@PathVariable Long id,
                                             @RequestBody(required = false) ProductDtos.OrderDecisionRequest request) {
        return productService.approve(id, request != null ? request : new ProductDtos.OrderDecisionRequest(null));
    }

    @PostMapping("/admin/product-orders/{id}/reject")
    public ProductDtos.OrderResponse reject(@PathVariable Long id,
                                            @RequestBody(required = false) ProductDtos.OrderDecisionRequest request) {
        return productService.reject(id, request != null ? request : new ProductDtos.OrderDecisionRequest(null));
    }
}
