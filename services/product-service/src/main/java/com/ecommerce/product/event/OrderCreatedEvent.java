package com.ecommerce.product.event;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Mirrors the payload published by order-service on order.created. */
public record OrderCreatedEvent(
        UUID orderId,
        UUID userId,
        BigDecimal totalAmount,
        List<Item> items
) {
    public record Item(UUID productId, int quantity, BigDecimal unitPrice) {}
}
