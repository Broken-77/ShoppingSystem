package com.wms.shoppingsys.order;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class OrderDtos {
    private OrderDtos() {
    }

    public record OrderResponse(
            Long id,
            String orderNo,
            Long userId,
            BigDecimal totalAmount,
            OrderStatus status,
            Instant createdAt,
            Instant paidAt,
            List<OrderItemResponse> items
    ) {
        public static OrderResponse from(Order order, List<OrderItem> items) {
            return new OrderResponse(
                    order.getId(),
                    order.getOrderNo(),
                    order.getUserId(),
                    order.getTotalAmount(),
                    order.getStatus(),
                    order.getCreatedAt(),
                    order.getPaidAt(),
                    items.stream().map(OrderItemResponse::from).toList()
            );
        }
    }

    public record OrderItemResponse(
            Long id,
            Long orderId,
            Long productId,
            String productName,
            BigDecimal productPrice,
            Integer quantity,
            BigDecimal subtotal
    ) {
        public static OrderItemResponse from(OrderItem item) {
            return new OrderItemResponse(
                    item.getId(),
                    item.getOrderId(),
                    item.getProductId(),
                    item.getProductName(),
                    item.getProductPrice(),
                    item.getQuantity(),
                    item.getSubtotal()
            );
        }
    }

    public record UpdateOrderStatusRequest(@NotNull OrderStatus status) {
    }
}
