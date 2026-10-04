package com.example.mohit.Ecomm.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.mohit.Ecomm.model.CartItem;
import com.example.mohit.Ecomm.model.Product;
import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.repo.CartItemRepository;
import com.example.mohit.Ecomm.repo.ProductRepository;
import com.example.mohit.Ecomm.repo.UserRepository;

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;


    // ==========================================
    // ADD PRODUCT TO CART
    // ==========================================

    public void addToCart(Long userId, Long productId, int quantity) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found with ID: " + userId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new RuntimeException("Product not found with ID: " + productId));

        if (quantity < 1) {
            quantity = 1;
        }

        Optional<CartItem> existingItem =
                cartItemRepository.findByUserAndProduct(user, product);

        if (existingItem.isPresent()) {

            CartItem cartItem = existingItem.get();

            cartItem.setQuantity(
                    cartItem.getQuantity() + quantity
            );

            cartItemRepository.save(cartItem);

        } else {

            CartItem cartItem = new CartItem();

            cartItem.setUser(user);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);

            cartItemRepository.save(cartItem);
        }
    }


    // ==========================================
    // GET USER CART
    // ==========================================

    @Transactional(readOnly = true)
    public List<CartItem> getCartItems(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found with ID: " + userId));

        return cartItemRepository.findByUser(user);
    }


    // ==========================================
    // UPDATE CART QUANTITY
    // ==========================================

    public void updateQuantity(
            Long userId,
            Long cartItemId,
            int quantity) {

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Cart item not found with ID: " + cartItemId
                        )
                );

        if (cartItem.getUser() == null ||
            cartItem.getUser().getId() == null ||
            !cartItem.getUser().getId().equals(userId)) {

            throw new RuntimeException("Unauthorized cart item");
        }

        if (quantity <= 0) {

            cartItemRepository.delete(cartItem);

        } else {

            cartItem.setQuantity(quantity);

            cartItemRepository.save(cartItem);
        }
    }


    // ==========================================
    // REMOVE CART ITEM
    // ==========================================

    public void removeItem(
            Long userId,
            Long cartItemId) {

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Cart item not found with ID: " + cartItemId
                        )
                );

        if (cartItem.getUser() == null ||
            cartItem.getUser().getId() == null ||
            !cartItem.getUser().getId().equals(userId)) {

            throw new RuntimeException("Unauthorized cart item");
        }

        cartItemRepository.delete(cartItem);
    }


    // ==========================================
    // CLEAR COMPLETE CART
    // ==========================================

    @Transactional
    public void clearCart(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found with ID: " + userId));

        cartItemRepository.deleteByUser(user);
    }
}