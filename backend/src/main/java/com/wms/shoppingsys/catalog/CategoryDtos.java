package com.wms.shoppingsys.catalog;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public final class CategoryDtos {
    private CategoryDtos() {
    }

    public record CategoryResponse(
            Long id,
            String name,
            Long parentId,
            Boolean enabled,
            Integer sortOrder
    ) {
        public static CategoryResponse from(Category category) {
            return new CategoryResponse(
                    category.getId(),
                    category.getName(),
                    category.getParentId(),
                    category.getEnabled(),
                    category.getSortOrder()
            );
        }
    }

    public record CategoryRequest(
            @NotBlank String name,
            Long parentId,
            Boolean enabled,
            @Min(0) Integer sortOrder
    ) {
        Category toCategory() {
            return new Category(name, parentId, enabled == null || enabled, sortOrder == null ? 0 : sortOrder);
        }
    }
}
