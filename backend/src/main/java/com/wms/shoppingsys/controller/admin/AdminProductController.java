package com.wms.shoppingsys.controller.admin;

import com.wms.shoppingsys.auth.AuthInterceptor;
import com.wms.shoppingsys.service.AuthService;
import com.wms.shoppingsys.auth.CurrentUser;
import com.wms.shoppingsys.entity.Product;
import com.wms.shoppingsys.dto.ProductDtos;
import com.wms.shoppingsys.service.ProductService;
import com.wms.shoppingsys.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {
    private final ProductService productService;
    private final AuthService authService;

    public AdminProductController(ProductService productService, AuthService authService) {
        this.productService = productService;
        this.authService = authService;
    }

    @GetMapping
    public ApiResponse<List<ProductDtos.ProductResponse>> list(HttpServletRequest request) {
        requireAdmin(request);
        List<ProductDtos.ProductResponse> products = productService.listAll().stream()
                .map(ProductDtos.ProductResponse::from)
                .toList();
        return ApiResponse.ok(products);
    }

    @PostMapping
    public ApiResponse<ProductDtos.ProductResponse> create(@Valid @RequestBody ProductDtos.ProductRequest body,
                                                           HttpServletRequest request) {
        requireAdmin(request);
        return ApiResponse.ok(ProductDtos.ProductResponse.from(productService.create(body)));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductDtos.ProductResponse> update(@PathVariable Long id,
                                                           @Valid @RequestBody ProductDtos.ProductRequest body,
                                                           HttpServletRequest request) {
        requireAdmin(request);
        return ApiResponse.ok(ProductDtos.ProductResponse.from(productService.update(id, body)));
    }

    @PostMapping("/{id}/on-sale")
    public ApiResponse<ProductDtos.ProductResponse> onSale(@PathVariable Long id, HttpServletRequest request) {
        requireAdmin(request);
        Product product = productService.markOnSale(id);
        return ApiResponse.ok(ProductDtos.ProductResponse.from(product));
    }

    @PostMapping("/{id}/off-sale")
    public ApiResponse<ProductDtos.ProductResponse> offSale(@PathVariable Long id, HttpServletRequest request) {
        requireAdmin(request);
        Product product = productService.markOffSale(id);
        return ApiResponse.ok(ProductDtos.ProductResponse.from(product));
    }

    private void requireAdmin(HttpServletRequest request) {
        authService.requireAdmin((CurrentUser) request.getAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE));
    }
}
