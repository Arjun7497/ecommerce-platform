package com.ecommerce.order.dto;

import com.ecommerce.order.model.Order;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {

    @NotNull
    private Order.OrderStatus status;
}
