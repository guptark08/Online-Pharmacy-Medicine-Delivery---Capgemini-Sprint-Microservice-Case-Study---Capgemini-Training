package com.orderanddelivery.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orderanddelivery.entities.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUserId(Long userId);

    Optional<CartItem> findByIdAndUserId(Long id, Long userId);

    Optional<CartItem> findByUserIdAndMedicineId(Long userId, Long medicineId);

    void deleteByUserId(Long userId);

    boolean existsByUserIdAndRequiresPrescriptionTrue(Long userId);
}
