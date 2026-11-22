package com.ecommerce.product.unit;

import com.ecommerce.product.event.OrderCreatedEvent;
import com.ecommerce.product.model.StockReservation;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.repository.StockReservationRepository;
import com.ecommerce.product.service.StockService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockService Unit Tests")
class StockServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private StockReservationRepository reservationRepository;
    @InjectMocks private StockService stockService;

    private final UUID orderId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    private OrderCreatedEvent event() {
        return new OrderCreatedEvent(orderId, UUID.randomUUID(), new BigDecimal("20.00"),
                List.of(new OrderCreatedEvent.Item(productId, 3, new BigDecimal("10.00"))));
    }

    @Test
    @DisplayName("reserve() decrements stock and records reservation")
    void reserve_decrementsStock() {
        when(reservationRepository.existsByOrderId(orderId)).thenReturn(false);
        when(productRepository.decrementStock(productId, 3)).thenReturn(1);

        stockService.reserve(event());

        verify(productRepository).decrementStock(productId, 3);
        verify(reservationRepository).save(any(StockReservation.class));
    }

    @Test
    @DisplayName("reserve() ignores duplicate delivery for the same order")
    void reserve_isIdempotent() {
        when(reservationRepository.existsByOrderId(orderId)).thenReturn(true);

        stockService.reserve(event());

        verify(productRepository, never()).decrementStock(any(), anyInt());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("release() restores stock and marks reservation released")
    void release_restoresStock() {
        var reservation = StockReservation.builder().orderId(orderId).productId(productId).quantity(3).build();
        when(reservationRepository.findByOrderIdAndReleasedFalse(orderId)).thenReturn(List.of(reservation));

        stockService.release(orderId);

        verify(productRepository).incrementStock(productId, 3);
        assertThat(reservation.isReleased()).isTrue();
    }

    @Test
    @DisplayName("release() is a no-op when already released")
    void release_isIdempotent() {
        when(reservationRepository.findByOrderIdAndReleasedFalse(orderId)).thenReturn(List.of());
        when(reservationRepository.existsByOrderId(orderId)).thenReturn(true);

        stockService.release(orderId);

        verify(productRepository, never()).incrementStock(any(), anyInt());
    }

    @Test
    @DisplayName("release() throws when order.created has not been processed yet, so the listener retries")
    void release_throwsWhenNoReservationYet() {
        when(reservationRepository.findByOrderIdAndReleasedFalse(orderId)).thenReturn(List.of());
        when(reservationRepository.existsByOrderId(orderId)).thenReturn(false);

        assertThatThrownBy(() -> stockService.release(orderId)).isInstanceOf(IllegalStateException.class);
    }
}
