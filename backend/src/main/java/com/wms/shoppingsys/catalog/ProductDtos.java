package com.wms.shoppingsys.catalog;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public final class ProductDtos {
    private ProductDtos() {
    }

    public record ProductResponse(
            Long id,
            Long categoryId,
            String name,
            String description,
            String brand,
            BigDecimal price,
            Integer stock,
            String imageUrl,
            ProductStatus status,
            Integer salesCount,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static ProductResponse from(Product product) {
            return new ProductResponse(
                    product.getId(),
                    product.getCategoryId(),
                    product.getName(),
                    product.getDescription(),
                    product.getBrand(),
                    product.getPrice(),
                    product.getStock(),
                    product.getImageUrl(),
                    product.getStatus(),
                    product.getSalesCount(),
                    product.getCreatedAt(),
                    product.getUpdatedAt()
            );
        }
    }

    public record ProductRequest(
            @NotNull Long categoryId,
            @NotBlank String name,
            String description,
            String brand,
            @NotNull @DecimalMin("0.00") BigDecimal price,
            @NotNull @Min(0) Integer stock,
            String imageUrl,
            ProductStatus status,
            @Min(0) Integer salesCount
    ) {
        Product toProduct() {
            return new Product(
                    categoryId,
                    name,
                    description,
                    brand,
                    price,
                    stock,
                    imageUrl,
                    status == null ? ProductStatus.OFF_SALE : status,
                    salesCount == null ? 0 : salesCount
            );
        }
    }
}
