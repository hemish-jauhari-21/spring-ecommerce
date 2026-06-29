package com.demo.ecommerce.controller;

import com.demo.ecommerce.dto.CartDTO;
import com.demo.ecommerce.dto.CartResponseDTO;
import com.demo.ecommerce.model.Cart;
import com.demo.ecommerce.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/ecommerce/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @PostMapping("/add")
    public CartResponseDTO createCart(@Valid @RequestBody CartDTO request) {
        return cartService.createCart(request);
    }

    @GetMapping("/{id}")
    public CartResponseDTO getCartById(@PathVariable Long id) {
        return cartService.getCartById(id);
    }

    @GetMapping("/{userId}")
    public Cart findByUserId(@PathVariable Long userId) {
        return cartService.findByUserId(userId);
    }
}
