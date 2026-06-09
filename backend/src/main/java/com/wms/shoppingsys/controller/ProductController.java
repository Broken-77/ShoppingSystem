package com.wms.shoppingsys.controller;

import com.wms.shoppingsys.dto.ProductDtos;
import com.wms.shoppingsys.entity.Product;
import com.wms.shoppingsys.service.ProductService;

import com.wms.shoppingsys.auth.AuthInterceptor;
import com.wms.shoppingsys.auth.CurrentUser;
import com.wms.shoppingsys.common.ApiResponse;
import com.wms.shoppingsys.service.BehaviorRecorder;
import com.wms.shoppingsys.service.RecommendationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;
    private final BehaviorRecorder behaviorRecorder;
    private final RecommendationService recommendationService;

    public ProductController(ProductService productService, BehaviorRecorder behaviorRecorder,
                             RecommendationService recommendationService) {
        this.productService = productService;
        this.behaviorRecorder = behaviorRecorder;
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public ApiResponse<List<ProductDtos.ProductResponse>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        List<ProductDtos.ProductResponse> products = productService.listOnSale(categoryId, keyword).stream()
                .map(ProductDtos.ProductResponse::from)
                .toList();
        return ApiResponse.ok(products);
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductDtos.ProductResponse> detail(@PathVariable Long id, HttpServletRequest request) {
        Product product = productService.getOnSaleProduct(id);
        CurrentUser currentUser = (CurrentUser) request.getAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE);
        if (currentUser != null) {
            behaviorRecorder.recordView(currentUser.id(), product.getId());
        }
        return ApiResponse.ok(ProductDtos.ProductResponse.from(product));
    }

    @GetMapping("/{id}/similar")
    public ApiResponse<List<ProductDtos.ProductResponse>> similar(@PathVariable Long id) {
        List<ProductDtos.ProductResponse> products = recommendationService.similarProducts(id).stream()
                .map(ProductDtos.ProductResponse::from)
                .toList();
        return ApiResponse.ok(products);
    }
}
