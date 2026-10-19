package com.example.mohit.Ecomm.repo;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.mohit.Ecomm.model.ReturnRequest;

@Repository
public interface ReturnRequestRepository
        extends JpaRepository<ReturnRequest, Long> {

    // Find all return requests submitted by a particular customer
    List<ReturnRequest> findByUser_IdOrderByRequestDateDesc(
            Long userId
    );

    // Retrieve return requests for the admin dashboard
    List<ReturnRequest> findAllByOrderByRequestDateDesc();

    // Check whether an item already has a request in one of the specified states
    boolean existsByOrderItem_IdAndStatusIn(
            Long orderItemId,
            Collection<String> statuses
    );
}