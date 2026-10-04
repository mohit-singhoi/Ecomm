package com.example.mohit.Ecomm.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.mohit.Ecomm.model.CartItem;
import com.example.mohit.Ecomm.model.Product;
import com.example.mohit.Ecomm.model.SavedItem;
import com.example.mohit.Ecomm.model.User;
import com.example.mohit.Ecomm.repo.CartItemRepository;
import com.example.mohit.Ecomm.repo.SavedItemRepository;
import com.example.mohit.Ecomm.repo.UserRepository;

@Service
public class SavedItemService {

    @Autowired
    private SavedItemRepository savedItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartItemRepository cartItemRepository;


    // =====================================================
    // SAVE CART ITEM FOR LATER
    // =====================================================

    @Transactional
    public void saveForLater(Long userId, Long cartItemId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found with ID: " + userId));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Cart item not found with ID: " + cartItemId
                        ));

        // Security check
        if (cartItem.getUser() == null
                || cartItem.getUser().getId() == null
                || !cartItem.getUser().getId().equals(userId)) {

            throw new RuntimeException(
                    "You cannot save this cart item"
            );
        }

        Product product = cartItem.getProduct();

        if (product == null) {
            throw new RuntimeException(
                    "Product not found for this cart item"
            );
        }

        // Check whether product is already saved
        SavedItem existing =
                savedItemRepository.findByUserAndProduct(user, product);

        if (existing != null) {

            int oldQuantity = existing.getQuantity();
            int cartQuantity = cartItem.getQuantity();

            existing.setQuantity(
                    oldQuantity + cartQuantity
            );

            savedItemRepository.save(existing);

        } else {

            SavedItem savedItem = new SavedItem();

            savedItem.setUser(user);
            savedItem.setProduct(product);
            savedItem.setQuantity(cartItem.getQuantity());

            savedItemRepository.save(savedItem);
        }

        // Remove product from cart after saving
        cartItemRepository.delete(cartItem);
    }


    // =====================================================
    // GET ALL SAVED ITEMS
    // =====================================================

    @Transactional(readOnly = true)
    public List<SavedItem> getSavedItems(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with ID: " + userId
                        ));

        return savedItemRepository.findByUser(user);
    }


    // =====================================================
    // REMOVE SAVED ITEM
    // =====================================================

    @Transactional
    public void removeSavedItem(
            Long userId,
            Long savedItemId) {

        SavedItem savedItem =
                savedItemRepository.findById(savedItemId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Saved item not found with ID: "
                                                + savedItemId
                                ));

        // Security check
        if (savedItem.getUser() == null
                || savedItem.getUser().getId() == null
                || !savedItem.getUser().getId().equals(userId)) {

            throw new RuntimeException(
                    "You cannot remove this saved item"
            );
        }

        savedItemRepository.delete(savedItem);
    }


    // =====================================================
    // MOVE SAVED ITEM TO CART
    // =====================================================

    @Transactional
    public void moveToCart(
            Long userId,
            Long savedItemId) {

        SavedItem savedItem =
                savedItemRepository.findById(savedItemId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Saved item not found with ID: "
                                                + savedItemId
                                ));

        // Security check
        if (savedItem.getUser() == null
                || savedItem.getUser().getId() == null
                || !savedItem.getUser().getId().equals(userId)) {

            throw new RuntimeException(
                    "You cannot move this saved item"
            );
        }

        Product product = savedItem.getProduct();

        if (product == null) {
            throw new RuntimeException(
                    "Product not found for this saved item"
            );
        }

        // Create or update cart item
        CartItem cartItem = createOrUpdateCartItem(
                userId,
                product,
                savedItem.getQuantity()
        );

        cartItemRepository.save(cartItem);

        // Remove from saved items
        savedItemRepository.delete(savedItem);
    }


    // =====================================================
    // CREATE OR UPDATE CART ITEM
    // =====================================================

    private CartItem createOrUpdateCartItem(
            Long userId,
            Product product,
            Integer quantity) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with ID: " + userId
                        ));

        /*
         * CartItemRepository returns Optional<CartItem>
         */
        Optional<CartItem> existing =
                cartItemRepository.findByUserAndProduct(
                        user,
                        product
                );

        if (existing.isPresent()) {

            CartItem cartItem = existing.get();

            int oldQuantity = cartItem.getQuantity();

            cartItem.setQuantity(
                    oldQuantity + quantity
            );

            return cartItem;
        }

        // Product does not exist in cart
        CartItem cartItem = new CartItem();

        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);

        return cartItem;
    }
}