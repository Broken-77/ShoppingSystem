package com.wms.shoppingsys.catalog;

import com.wms.shoppingsys.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ApiResponse<List<CategoryDtos.CategoryResponse>> list() {
        List<CategoryDtos.CategoryResponse> categories = categoryService.listEnabled().stream()
                .map(CategoryDtos.CategoryResponse::from)
                .toList();
        return ApiResponse.ok(categories);
    }
}
