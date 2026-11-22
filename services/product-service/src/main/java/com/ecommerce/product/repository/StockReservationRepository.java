package com.ecommerce.product.repository;

import com.ecommerce.product.model.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StockReservationRepository extends JpaRepository<StockReservation, UUID> {

    boolean existsByOrderId(UUID orderId);

    List<StockReservation> findByOrderIdAndReleasedFalse(UUID orderId);
}
