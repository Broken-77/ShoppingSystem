package com.wms.shoppingsys.dto;

import com.wms.shoppingsys.entity.CartItem;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public final class CartDtos {
    private CartDtos() {
    }

    public record CartItemResponse(
            Long id,
            Long userId,
            Long productId,
            Integer quantity,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static CartItemResponse from(CartItem item) {
            return new CartItemResponse(
                    item.getId(),
                    item.getUserId(),
                    item.getProductId(),
                    item.getQuantity(),
                    item.getCreatedAt(),
                    item.getUpdatedAt()
            );
        }
    }

    public record AddCartItemRequest(
            @NotNull Long productId,
            @NotNull @Min(1) Integer quantity
    ) {
    }

    public record UpdateCartItemRequest(
            @NotNull @Min(1) Integer quantity
    ) {
    }
}
