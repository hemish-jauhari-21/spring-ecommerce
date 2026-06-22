package com.demo.ecommerce.service;

import com.demo.ecommerce.dto.CartDTO;
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

    public Cart createCart(CartDTO request) {
        User user = userRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = new Cart();

        cart.setUser(user);

        return cartRepository.save(cart);
    }

    public Cart getCartById(Long id) {
        return cartRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
    }
}
