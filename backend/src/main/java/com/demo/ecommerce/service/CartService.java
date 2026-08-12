package com.demo.ecommerce.service;

import com.demo.ecommerce.dto.CartResponseDTO;
import com.demo.ecommerce.dto.UserResponseDTO;
import com.demo.ecommerce.model.Cart;
import com.demo.ecommerce.model.User;
import com.demo.ecommerce.repository.CartRepository;
import com.demo.ecommerce.repository.UserRepository;
import com.demo.ecommerce.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser(Authentication authentication) {
        return SecurityUtils.getCurrentUser(authentication, userRepository);
    }

    public CartResponseDTO createCart(Authentication authentication) {
        User user = getCurrentUser(authentication);

        return cartRepository.findByUserId(user.getId())
                .map(this::toResponse)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return toResponse(cartRepository.save(cart));
                });
    }

    public CartResponseDTO getMyCart(Authentication authentication) {
        User user = getCurrentUser(authentication);

        return cartRepository.findByUserId(user.getId())
                .map(this::toResponse)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return toResponse(cartRepository.save(cart));
                });
    }

    public CartResponseDTO getCartById(Long id, Authentication authentication) {
        Cart cart = cartRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (!isOwner(cart, authentication)
                && !SecurityUtils.hasRole(authentication, "ROLE_ADMIN")) {
            throw new AccessDeniedException("You do not have access to this cart");
        }

        return toResponse(cart);
    }

    public Cart getOrCreateCart(Authentication authentication) {
        User user = getCurrentUser(authentication);

        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return cartRepository.save(cart);
                });
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

        return new CartResponseDTO(
                cart.getId(),
                cart.getTotalAmount(),
                userResponseDTO
        );
    }
}