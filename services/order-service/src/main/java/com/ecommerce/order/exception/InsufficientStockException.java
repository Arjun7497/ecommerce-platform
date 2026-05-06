package com.ecommerce.order.exception;

import java.util.UUID;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(UUID productId, int available) {
        super(String.format("Insufficient stock for product %s. Available: %d", productId, available));
    }
}
