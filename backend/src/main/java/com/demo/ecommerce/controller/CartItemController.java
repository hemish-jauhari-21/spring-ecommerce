package com.demo.ecommerce.controller;

import com.demo.ecommerce.dto.CartItemDTO;
import com.demo.ecommerce.dto.CartItemResponseDTO;
import com.demo.ecommerce.model.CartItem;
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

    @PostMapping("/add")
    public CartItemResponseDTO addItem(@Valid @RequestBody CartItemDTO request,
                                       Authentication authentication) {
        return cartItemService.addItem(request, authentication);
    }

    @GetMapping("/me")
    public List<CartItem> getMyItems(Authentication authentication) {
        return cartItemService.getMyItems(authentication);
    }

    @GetMapping("/all")
    public List<CartItemResponseDTO> getAllItems() {
        return cartItemService.getAllItems();
    }

    @GetMapping("/cart/{cartId}")
    public List<CartItem> getItems(@PathVariable Long cartId,
                                   Authentication authentication) {
        return cartItemService.getItemsByCartId(cartId, authentication);
    }

    @DeleteMapping("/{id}")
    public String deleteItem(@PathVariable Long id,
                             Authentication authentication) {
        return cartItemService.deleteItem(id, authentication);
    }

    @PutMapping("/update/{id}")
    public CartItemResponseDTO updateQuantity(@PathVariable Long id,
                                              @RequestParam Integer quantity,
                                              Authentication authentication) {
        return cartItemService.updateQuantity(id, quantity, authentication);
    }
}