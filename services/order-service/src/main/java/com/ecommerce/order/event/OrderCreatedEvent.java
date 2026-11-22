package com.ecommerce.order.event;

import com.ecommerce.order.model.Order;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        UUID userId,
        BigDecimal totalAmount,
        List<Item> items
) {
    public record Item(UUID productId, int quantity, BigDecimal unitPrice) {}

    public static OrderCreatedEvent from(Order order) {
        return new OrderCreatedEvent(
                order.getId(),
                order.getUserId(),
                order.getTotalAmount(),
                order.getItems().stream()
                        .map(i -> new Item(i.getProductId(), i.getQuantity(), i.getUnitPrice()))
                        .toList());
    }
}
