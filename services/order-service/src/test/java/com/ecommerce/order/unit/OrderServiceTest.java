package com.ecommerce.order.unit;

import com.ecommerce.order.client.ProductClient;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.exception.InsufficientStockException;
import com.ecommerce.order.exception.OrderNotFoundException;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.service.OrderService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ProductClient productClient;
    @InjectMocks private OrderService orderService;

    @Nested
    @DisplayName("createOrder()")
    class CreateOrder {

        @Test
        @DisplayName("should create order with correct total when stock is sufficient")
        void shouldCreateOrder_whenStockSufficient() {
            var productId = UUID.randomUUID();
            var product = buildProduct(productId, "Laptop", new BigDecimal("999.99"), 10);
            var request = buildOrderRequest(productId, 2);
            var savedOrder = buildSavedOrder(request.getUserId(), new BigDecimal("1999.98"));

            when(productClient.getProduct(productId)).thenReturn(product);
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

            var response = orderService.createOrder(request);

            assertThat(response.getTotalAmount()).isEqualByComparingTo("1999.98");
            assertThat(response.getStatus()).isEqualTo(Order.OrderStatus.PENDING);
        }

        @Test
        @DisplayName("should throw InsufficientStockException when stock is not enough")
        void shouldThrow_whenInsufficientStock() {
            var productId = UUID.randomUUID();
            var product = buildProduct(productId, "Laptop", new BigDecimal("999.99"), 1);
            var request = buildOrderRequest(productId, 5);

            when(productClient.getProduct(productId)).thenReturn(product);

            assertThatThrownBy(() -> orderService.createOrder(request))
                    .isInstanceOf(InsufficientStockException.class)
                    .hasMessageContaining("Insufficient stock");
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("should calculate total correctly for multiple items")
        void shouldCalculateTotal_forMultipleItems() {
            var productId1 = UUID.randomUUID();
            var productId2 = UUID.randomUUID();
            var p1 = buildProduct(productId1, "Mouse", new BigDecimal("29.99"), 10);
            var p2 = buildProduct(productId2, "Keyboard", new BigDecimal("49.99"), 10);

            var request = new CreateOrderRequest();
            request.setUserId(UUID.randomUUID());
            request.setShippingAddress("123 Test St");
            var item1 = new CreateOrderRequest.OrderItemRequest();
            item1.setProductId(productId1);
            item1.setQuantity(2);
            var item2 = new CreateOrderRequest.OrderItemRequest();
            item2.setProductId(productId2);
            item2.setQuantity(1);
            request.setItems(List.of(item1, item2));

            when(productClient.getProduct(productId1)).thenReturn(p1);
            when(productClient.getProduct(productId2)).thenReturn(p2);

            var savedOrder = buildSavedOrder(request.getUserId(), new BigDecimal("109.97"));
            when(orderRepository.save(any())).thenReturn(savedOrder);

            var response = orderService.createOrder(request);

            assertThat(response.getTotalAmount()).isEqualByComparingTo("109.97");
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("should throw OrderNotFoundException when order not found")
        void shouldThrow_whenOrderNotFound() {
            var id = UUID.randomUUID();
            when(orderRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.findById(id))
                    .isInstanceOf(OrderNotFoundException.class);
        }
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private ProductClient.ProductDto buildProduct(UUID id, String name, BigDecimal price, int stock) {
        var dto = new ProductClient.ProductDto();
        dto.setId(id);
        dto.setName(name);
        dto.setPrice(price);
        dto.setStockQuantity(stock);
        dto.setActive(true);
        return dto;
    }

    private CreateOrderRequest buildOrderRequest(UUID productId, int quantity) {
        var request = new CreateOrderRequest();
        request.setUserId(UUID.randomUUID());
        request.setShippingAddress("123 Main St");
        var item = new CreateOrderRequest.OrderItemRequest();
        item.setProductId(productId);
        item.setQuantity(quantity);
        request.setItems(List.of(item));
        return request;
    }

    private Order buildSavedOrder(UUID userId, BigDecimal total) {
        return Order.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .totalAmount(total)
                .shippingAddress("123 Main St")
                .status(Order.OrderStatus.PENDING)
                .items(new ArrayList<>())
                .build();
    }
}
