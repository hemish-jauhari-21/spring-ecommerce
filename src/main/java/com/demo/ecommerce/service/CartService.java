package com.demo.ecommerce.service;

import com.demo.ecommerce.dto.CartDTO;
import com.demo.ecommerce.dto.CartResponseDTO;
import com.demo.ecommerce.dto.UserResponseDTO;
import com.demo.ecommerce.model.Cart;
import com.demo.ecommerce.model.User;
import com.demo.ecommerce.repository.CartRepository;
import com.demo.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    public CartResponseDTO createCart(CartDTO request) {
        User user = userRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = new Cart();

        cart.setUser(user);

        Cart savedCart = cartRepository.save(cart);

        UserResponseDTO userResponseDTO = new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail()
        );

        return new CartResponseDTO(
                savedCart.getId(),
                savedCart.getTotalAmount(),
                userResponseDTO
        );
    }

    public CartResponseDTO getCartById(Long id) {
        Cart cart = cartRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

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

    public Cart findByUserId(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        return cart;
    }
}
