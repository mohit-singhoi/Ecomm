package com.example.mohit.Ecomm.repo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.mohit.Ecomm.model.ReturnRequest;

@Repository
public interface ReturnRequestRepository
        extends JpaRepository<ReturnRequest, Long> {

    // =====================================================
    // CUSTOMER RETURN REQUESTS
    // =====================================================

    @EntityGraph(attributePaths = {
            "user",
            "orderItem",
            "orderItem.product",
            "orderItem.order"
    })
    List<ReturnRequest> findByUser_IdOrderByRequestDateDesc(
            Long userId
    );

    // =====================================================
    // ALL REQUESTS FOR THE ADMIN DASHBOARD
    // =====================================================

    @EntityGraph(attributePaths = {
            "user",
            "orderItem",
            "orderItem.product",
            "orderItem.order"
    })
    List<ReturnRequest> findAllByOrderByRequestDateDesc();

    // =====================================================
    // FIND ONE REQUEST WITH DETAILS
    // =====================================================

    @Query("""
            SELECT r
            FROM ReturnRequest r
            JOIN FETCH r.user
            JOIN FETCH r.orderItem oi
            JOIN FETCH oi.product
            JOIN FETCH oi.order
            WHERE r.id = :requestId
            """)
    Optional<ReturnRequest> findDetailedById(
            @Param("requestId") Long requestId
    );

    // =====================================================
    // CHECK FOR EXISTING RETURN REQUESTS
    // =====================================================

    boolean existsByOrderItem_IdAndStatusIn(
            Long orderItemId,
            Collection<String> statuses
    );
}