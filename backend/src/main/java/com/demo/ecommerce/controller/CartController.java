package com.demo.ecommerce.controller;

import com.demo.ecommerce.dto.CartResponseDTO;
import com.demo.ecommerce.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ecommerce/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @PostMapping("/add")
    public CartResponseDTO createCart(Authentication authentication) {
        return cartService.createCart(authentication);
    }

    @GetMapping("/me")
    public CartResponseDTO getMyCart(Authentication authentication) {
        return cartService.getMyCart(authentication);
    }

    @GetMapping("/{id}")
    public CartResponseDTO getCartById(@PathVariable Long id,
                                       Authentication authentication) {
        return cartService.getCartById(id, authentication);
    }
}