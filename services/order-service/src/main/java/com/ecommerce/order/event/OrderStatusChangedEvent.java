package com.ecommerce.order.event;

import com.ecommerce.order.model.Order.OrderStatus;

import java.util.UUID;

public record OrderStatusChangedEvent(
        UUID orderId,
        UUID userId,
        OrderStatus previousStatus,
        OrderStatus newStatus
) {}
