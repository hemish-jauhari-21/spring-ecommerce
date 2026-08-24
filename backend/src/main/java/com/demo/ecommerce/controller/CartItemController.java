package com.demo.ecommerce.controller;

import com.demo.ecommerce.dto.CartItemDTO;
import com.demo.ecommerce.dto.CartItemResponseDTO;
import com.demo.ecommerce.service.CartItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ecommerce/cart-item")
public class CartItemController {

    @Autowired
    private CartItemService cartItemService;

    // Add product to current user's cart
    @PostMapping("/add")
    public CartItemResponseDTO addItem(
            @Valid @RequestBody CartItemDTO request,
            Authentication authentication) {

        return cartItemService.addItem(request, authentication);
    }

    // Get current user's cart items
    @GetMapping("/me")
    public List<CartItemResponseDTO> getMyItems(
            Authentication authentication) {

        return cartItemService.getMyItems(authentication);
    }

    // Get all cart items
    // This endpoint should be ADMIN-only in SecurityConfig
    @GetMapping("/all")
    public List<CartItemResponseDTO> getAllItems() {

        return cartItemService.getAllItems();
    }

    // Get items of a specific cart
    // Ownership is checked in the service
    @GetMapping("/cart/{cartId}")
    public List<CartItemResponseDTO> getItems(
            @PathVariable Long cartId,
            Authentication authentication) {

        return cartItemService.getItemsByCartId(
                cartId,
                authentication
        );
    }

    // Delete cart item
    @DeleteMapping("/{id}")
    public String deleteItem(
            @PathVariable Long id,
            Authentication authentication) {

        return cartItemService.deleteItem(
                id,
                authentication
        );
    }

    // Update cart item quantity
    @PutMapping("/update/{id}")
    public CartItemResponseDTO updateQuantity(
            @PathVariable Long id,
            @RequestParam Integer quantity,
            Authentication authentication) {

        return cartItemService.updateQuantity(
                id,
                quantity,
                authentication
        );
    }
}