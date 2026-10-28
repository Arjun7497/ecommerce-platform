package com.ecommerce.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateOrderRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotEmpty(message = "Order must contain at least one item")
    private List<OrderItemRequest> items;

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    @Data
    public static class OrderItemRequest {
        @NotNull
        private UUID productId;
        @NotNull
        private Integer quantity;
    }
}
