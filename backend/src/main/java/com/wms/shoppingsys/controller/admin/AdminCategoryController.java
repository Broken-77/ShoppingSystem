package com.wms.shoppingsys.controller.admin;

import com.wms.shoppingsys.auth.AuthInterceptor;
import com.wms.shoppingsys.service.AuthService;
import com.wms.shoppingsys.auth.CurrentUser;
import com.wms.shoppingsys.entity.Category;
import com.wms.shoppingsys.dto.CategoryDtos;
import com.wms.shoppingsys.service.CategoryService;
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
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {
    private final CategoryService categoryService;
    private final AuthService authService;

    public AdminCategoryController(CategoryService categoryService, AuthService authService) {
        this.categoryService = categoryService;
        this.authService = authService;
    }

    @GetMapping
    public ApiResponse<List<CategoryDtos.CategoryResponse>> list(HttpServletRequest request) {
        requireAdmin(request);
        List<CategoryDtos.CategoryResponse> categories = categoryService.listAll().stream()
                .map(CategoryDtos.CategoryResponse::from)
                .toList();
        return ApiResponse.ok(categories);
    }

    @PostMapping
    public ApiResponse<CategoryDtos.CategoryResponse> create(@Valid @RequestBody CategoryDtos.CategoryRequest body,
                                                             HttpServletRequest request) {
        requireAdmin(request);
        return ApiResponse.ok(CategoryDtos.CategoryResponse.from(categoryService.create(body)));
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryDtos.CategoryResponse> update(@PathVariable Long id,
                                                             @Valid @RequestBody CategoryDtos.CategoryRequest body,
                                                             HttpServletRequest request) {
        requireAdmin(request);
        return ApiResponse.ok(CategoryDtos.CategoryResponse.from(categoryService.update(id, body)));
    }

    @PostMapping("/{id}/disable")
    public ApiResponse<CategoryDtos.CategoryResponse> disable(@PathVariable Long id, HttpServletRequest request) {
        requireAdmin(request);
        Category category = categoryService.disable(id);
        return ApiResponse.ok(CategoryDtos.CategoryResponse.from(category));
    }

    private void requireAdmin(HttpServletRequest request) {
        authService.requireAdmin((CurrentUser) request.getAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE));
    }
}
