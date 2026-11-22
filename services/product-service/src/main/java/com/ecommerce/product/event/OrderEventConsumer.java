package com.ecommerce.product.event;

import com.ecommerce.product.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final StockService stockService;

    @KafkaListener(
            topics = "${kafka.topics.order-created}",
            properties = "spring.json.value.default.type=com.ecommerce.product.event.OrderCreatedEvent")
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Received order.created for order {}", event.orderId());
        stockService.reserve(event);
    }

    @KafkaListener(
            topics = "${kafka.topics.order-status-changed}",
            properties = "spring.json.value.default.type=com.ecommerce.product.event.OrderStatusChangedEvent")
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        if ("CANCELLED".equals(event.newStatus())) {
            log.info("Received cancellation for order {}", event.orderId());
            stockService.release(event.orderId());
        }
    }
}
