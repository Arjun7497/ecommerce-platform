package com.ecommerce.order.client;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class ProductClient {

    private final RestTemplate restTemplate;
    private final String productServiceUrl;

    public ProductClient(RestTemplate restTemplate,
                         @Value("${product.service.url:http://localhost:8082}") String productServiceUrl) {
        this.restTemplate = restTemplate;
        this.productServiceUrl = productServiceUrl;
    }

    public ProductDto getProduct(UUID productId) {
        return restTemplate.getForObject(
                productServiceUrl + "/api/v1/products/" + productId,
                ProductDto.class
        );
    }

    @Data
    public static class ProductDto {
        private UUID id;
        private String name;
        private BigDecimal price;
        private Integer stockQuantity;
        private boolean active;
    }
}
