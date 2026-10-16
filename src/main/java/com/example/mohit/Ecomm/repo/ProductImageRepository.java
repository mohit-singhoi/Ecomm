package com.example.mohit.Ecomm.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mohit.Ecomm.model.ProductImage;

public interface ProductImageRepository
        extends JpaRepository<ProductImage, Long> {

    // Get all images for a product in display order
    List<ProductImage> findByProduct_IdOrderByDisplayOrderAsc(Long productId);

    // Get the primary image for a product
    Optional<ProductImage> findByProduct_IdAndPrimaryImageTrue(Long productId);

    // Count images associated with a product
    long countByProduct_Id(Long productId);

    // Find all images for a product before deletion
    List<ProductImage> findByProduct_Id(Long productId);
}