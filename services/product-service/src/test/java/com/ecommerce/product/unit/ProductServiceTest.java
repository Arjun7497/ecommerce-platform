package com.ecommerce.product.unit;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.service.ProductService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @InjectMocks private ProductService productService;

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should create product and return response")
        void shouldCreateProduct() {
            var request = buildRequest("Laptop", "Electronics", new BigDecimal("999.99"), 50);
            var savedProduct = buildProduct(request);
            when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

            var response = productService.create(request);

            assertThat(response.getName()).isEqualTo("Laptop");
            assertThat(response.getPrice()).isEqualByComparingTo("999.99");
            assertThat(response.getStockQuantity()).isEqualTo(50);
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("should default active to true on creation")
        void shouldDefaultActiveToTrue() {
            var request = buildRequest("Phone", "Electronics", new BigDecimal("499.99"), 10);
            var savedProduct = buildProduct(request);
            when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

            var response = productService.create(request);

            assertThat(response.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("should return paginated products")
        void shouldReturnPagedProducts() {
            var pageable = PageRequest.of(0, 10);
            var product = buildProduct(buildRequest("Item", "Cat", new BigDecimal("10.00"), 5));
            var page = new PageImpl<>(List.of(product), pageable, 1);
            when(productRepository.findByActiveTrue(pageable)).thenReturn(page);

            var result = productService.findAll(pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Item");
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("should return product when found")
        void shouldReturnProduct_whenFound() {
            var id = UUID.randomUUID();
            var product = buildProduct(buildRequest("Book", "Books", new BigDecimal("29.99"), 100));
            product.setId(id);
            when(productRepository.findById(id)).thenReturn(Optional.of(product));

            var response = productService.findById(id);

            assertThat(response.getId()).isEqualTo(id);
            assertThat(response.getName()).isEqualTo("Book");
        }

        @Test
        @DisplayName("should throw ProductNotFoundException when not found")
        void shouldThrow_whenNotFound() {
            var id = UUID.randomUUID();
            when(productRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.findById(id))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("should soft delete product by setting active=false")
        void shouldSoftDelete() {
            var id = UUID.randomUUID();
            var product = buildProduct(buildRequest("TV", "Electronics", new BigDecimal("599.99"), 5));
            product.setId(id);
            when(productRepository.findById(id)).thenReturn(Optional.of(product));
            when(productRepository.save(any())).thenReturn(product);

            productService.delete(id);

            ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
            verify(productRepository).save(captor.capture());
            assertThat(captor.getValue().isActive()).isFalse();
        }
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private ProductRequest buildRequest(String name, String category, BigDecimal price, int stock) {
        var req = new ProductRequest();
        req.setName(name);
        req.setCategory(category);
        req.setPrice(price);
        req.setStockQuantity(stock);
        req.setDescription("Test description");
        return req;
    }

    private Product buildProduct(ProductRequest req) {
        return Product.builder()
                .id(UUID.randomUUID())
                .name(req.getName())
                .description(req.getDescription())
                .price(req.getPrice())
                .stockQuantity(req.getStockQuantity())
                .category(req.getCategory())
                .active(true)
                .build();
    }
}
