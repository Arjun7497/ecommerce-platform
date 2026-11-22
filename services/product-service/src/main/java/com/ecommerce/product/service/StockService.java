package com.ecommerce.product.service;

import com.ecommerce.product.event.OrderCreatedEvent;
import com.ecommerce.product.model.StockReservation;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.repository.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final ProductRepository productRepository;
    private final StockReservationRepository reservationRepository;

    /** Decrements stock for every item and records the reservation. Idempotent per order. */
    @Transactional
    public void reserve(OrderCreatedEvent event) {
        if (reservationRepository.existsByOrderId(event.orderId())) {
            log.info("Order {} already reserved, skipping duplicate", event.orderId());
            return;
        }
        for (var item : event.items()) {
            int updated = productRepository.decrementStock(item.productId(), item.quantity());
            if (updated == 0) {
                log.warn("Order {}: product {} not found, nothing decremented", event.orderId(), item.productId());
            }
            reservationRepository.save(StockReservation.builder()
                    .orderId(event.orderId())
                    .productId(item.productId())
                    .quantity(item.quantity())
                    .build());
        }
    }

    /**
     * Restores stock reserved for an order. Idempotent.
     * Throws if no reservation exists yet (cancel can overtake order.created across topics),
     * so the listener's retry policy gives order.created time to be processed.
     */
    @Transactional
    public void release(UUID orderId) {
        List<StockReservation> active = reservationRepository.findByOrderIdAndReleasedFalse(orderId);
        if (active.isEmpty()) {
            if (!reservationRepository.existsByOrderId(orderId)) {
                throw new IllegalStateException("No reservation yet for cancelled order " + orderId);
            }
            log.info("Order {} already released, skipping", orderId);
            return;
        }
        for (var reservation : active) {
            productRepository.incrementStock(reservation.getProductId(), reservation.getQuantity());
            reservation.setReleased(true);
        }
    }
}
