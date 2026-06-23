package com.demo.ecommerce.controller;

import com.demo.ecommerce.dto.CartItemDTO;
import com.demo.ecommerce.model.CartItem;
import com.demo.ecommerce.repository.CartItemRepository;
import com.demo.ecommerce.service.CartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ecommerce/cart-item")
public class CartItemController {
    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private CartItemRepository cartItemRepository;

    @PostMapping("/add")
    public CartItem addItem(@RequestBody CartItemDTO request) {
        return cartItemService.addItem(request);
    }

    @GetMapping("/all")
    public List<CartItem> getAllItems() {
        return cartItemService.getAllItems();
    }

    @GetMapping("/cart/{cartId}")
    public List<CartItem> getItems(
            @PathVariable Long cartId) {

        return cartItemRepository.findByCartId(cartId);
    }

    @DeleteMapping("/{id}")
    public String deleteItem(@PathVariable Long id) {
        return cartItemService.deleteItem(id);
    }
}
