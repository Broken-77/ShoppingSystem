package com.wms.shoppingsys.catalog;

import com.wms.shoppingsys.common.BusinessException;
import com.wms.shoppingsys.common.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<Category> listEnabled() {
        return categoryRepository.findByEnabledTrueOrderBySortOrderAsc();
    }

    @Transactional(readOnly = true)
    public List<Category> listAll() {
        return categoryRepository.findAll();
    }

    @Transactional
    public Category create(CategoryDtos.CategoryRequest request) {
        return categoryRepository.save(request.toCategory());
    }

    @Transactional
    public Category update(Long id, CategoryDtos.CategoryRequest request) {
        Category category = getCategory(id);
        category.update(
                request.name(),
                request.parentId(),
                request.enabled() == null ? category.getEnabled() : request.enabled(),
                request.sortOrder() == null ? category.getSortOrder() : request.sortOrder()
        );
        return category;
    }

    @Transactional
    public Category disable(Long id) {
        Category category = getCategory(id);
        category.disable();
        return category;
    }

    private Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "分类不存在"));
    }
}
