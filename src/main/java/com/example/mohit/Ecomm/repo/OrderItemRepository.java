package com.example.mohit.Ecomm.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.mohit.Ecomm.model.OrderItem;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface OrderItemRepository
        extends JpaRepository<OrderItem, Long> {

    boolean existsByProduct_Id(Long productId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT oi FROM OrderItem oi WHERE oi.id = :orderItemId")
    Optional<OrderItem> findByIdForUpdate(
            @Param("orderItemId") Long orderItemId);
    
}