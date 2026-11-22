package com.ecommerce.product.event;

import java.util.UUID;

/** Mirrors the payload published by order-service on order.status-changed. */
public record OrderStatusChangedEvent(
        UUID orderId,
        UUID userId,
        String previousStatus,
        String newStatus
) {}
