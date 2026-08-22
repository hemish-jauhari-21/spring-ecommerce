package com.demo.ecommerce.service;

import com.demo.ecommerce.dto.CartResponseDTO;
import com.demo.ecommerce.dto.UserResponseDTO;
import com.demo.ecommerce.exception.ResourceNotFoundException;
import com.demo.ecommerce.model.Cart;
import com.demo.ecommerce.model.CartItem;
import com.demo.ecommerce.model.User;
import com.demo.ecommerce.repository.CartItemRepository;
import com.demo.ecommerce.repository.CartRepository;
import com.demo.ecommerce.repository.UserRepository;
import com.demo.ecommerce.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    private User getCurrentUser(Authentication authentication) {
        return SecurityUtils.getCurrentUser(authentication, userRepository);
    }

    // --------------------------------------------------
    // Server-authoritative cart total.
    // product.price * quantity for every item in the cart.
    // The frontend total is never trusted.
    // --------------------------------------------------

    public double calculateCartTotal(Cart cart) {

        List<CartItem> items =
                cartItemRepository.findByCartId(cart.getId());

        return items.stream()
                .mapToDouble(item ->
                        item.getProduct().getPrice()
                                * item.getQuantity())
                .sum();
    }

    // Recalculate and persist the cart total.
    // Must be called whenever cart contents change:
    // ADD / UPDATE / DELETE / CHECKOUT.

    @Transactional
    public void recalculateTotal(Cart cart) {
        cart.setTotalAmount(calculateCartTotal(cart));
        cartRepository.save(cart);
    }

    public CartResponseDTO createCart(Authentication authentication) {
        User user = getCurrentUser(authentication);

        return cartRepository.findByUserId(user.getId())
                .map(this::toResponse)
                .orElseGet(() -> toResponse(createNewCart(user)));
    }

    public CartResponseDTO getMyCart(Authentication authentication) {
        User user = getCurrentUser(authentication);

        return cartRepository.findByUserId(user.getId())
                .map(this::toResponse)
                .orElseGet(() -> toResponse(createNewCart(user)));
    }

    public CartResponseDTO getCartById(Long id, Authentication authentication) {
        Cart cart = cartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + id));

        if (!isOwner(cart, authentication)
                && !SecurityUtils.hasRole(authentication, "ROLE_ADMIN")) {
            throw new AccessDeniedException("You do not have access to this cart");
        }

        return toResponse(cart);
    }

    public Cart getOrCreateCart(Authentication authentication) {
        User user = getCurrentUser(authentication);

        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> createNewCart(user));
    }

    private Cart createNewCart(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setTotalAmount(0.0);
        return cartRepository.save(cart);
    }

    private boolean isOwner(Cart cart, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        return cart.getUser() != null
                && cart.getUser().getId().equals(currentUser.getId());
    }

    private CartResponseDTO toResponse(Cart cart) {
        User user = cart.getUser();

        UserResponseDTO userResponseDTO = new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail()
        );

        Double totalAmount = cart.getTotalAmount();

        // Legacy rows may hold a null total; expose the
        // freshly computed value instead of null.
        double responseTotal = totalAmount != null
                ? totalAmount
                : calculateCartTotal(cart);

        return new CartResponseDTO(
                cart.getId(),
                responseTotal,
                userResponseDTO
        );
    }
}
