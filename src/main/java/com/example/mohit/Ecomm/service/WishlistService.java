package com.example.mohit.Ecomm.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.mohit.Ecomm.model.Product;
import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.model.WishlistItem;
import com.example.mohit.Ecomm.repo.ProductRepository;
import com.example.mohit.Ecomm.repo.UserRepository;
import com.example.mohit.Ecomm.repo.WishlistItemRepository;

@Service
public class WishlistService {

    @Autowired
    private WishlistItemRepository wishlistItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public void addToWishlist(
            Long userId,
            Long productId) {

        User user =
                userRepository.findById(userId)
                .orElseThrow(() ->
                    new RuntimeException("User not found.")
                );

        Product product =
                productRepository.findById(productId)
                .orElseThrow(() ->
                    new RuntimeException("Product not found.")
                );

        boolean alreadyExists =
                wishlistItemRepository
                .findByUserAndProduct(user, product)
                .isPresent();

        if (alreadyExists) {
            return;
        }

        WishlistItem wishlistItem =
                new WishlistItem();

        wishlistItem.setUser(user);
        wishlistItem.setProduct(product);

        wishlistItemRepository.save(wishlistItem);
    }

    @Transactional(readOnly = true)
    public List<WishlistItem> getWishlist(Long userId) {

        User user =
                userRepository.findById(userId)
                .orElseThrow(() ->
                    new RuntimeException("User not found.")
                );

        return wishlistItemRepository.findByUser(user);
    }

    @Transactional
    public void removeFromWishlist(
            Long userId,
            Long wishlistItemId) {

        WishlistItem wishlistItem =
                wishlistItemRepository.findById(
                    wishlistItemId
                )
                .orElseThrow(() ->
                    new RuntimeException(
                        "Wishlist item not found."
                    )
                );

        if (wishlistItem.getUser() == null
                || wishlistItem.getUser().getId() == null
                || !wishlistItem.getUser()
                    .getId()
                    .equals(userId)) {

            throw new RuntimeException(
                "You cannot remove this wishlist item."
            );
        }

        wishlistItemRepository.delete(wishlistItem);
    }

    @Transactional
    public void removeProductFromWishlist(
            Long userId,
            Long productId) {

        User user =
                userRepository.findById(userId)
                .orElseThrow(() ->
                    new RuntimeException("User not found.")
                );

        Product product =
                productRepository.findById(productId)
                .orElseThrow(() ->
                    new RuntimeException("Product not found.")
                );

        wishlistItemRepository
            .deleteByUserAndProduct(user, product);
    }

    @Transactional(readOnly = true)
    public boolean isInWishlist(
            Long userId,
            Long productId) {

        User user =
                userRepository.findById(userId)
                .orElseThrow(() ->
                    new RuntimeException("User not found.")
                );

        Product product =
                productRepository.findById(productId)
                .orElseThrow(() ->
                    new RuntimeException("Product not found.")
                );

        return wishlistItemRepository
                .findByUserAndProduct(user, product)
                .isPresent();
    }

    @Transactional(readOnly = true)
    public long getWishlistCount(Long userId) {

        User user =
                userRepository.findById(userId)
                .orElseThrow(() ->
                    new RuntimeException("User not found.")
                );

        return wishlistItemRepository.countByUser(user);
    }
}