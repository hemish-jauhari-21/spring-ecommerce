package com.demo.ecommerce.service;

import com.demo.ecommerce.dto.CartItemDTO;
import com.demo.ecommerce.model.Cart;
import com.demo.ecommerce.model.CartItem;
import com.demo.ecommerce.model.Product;
import com.demo.ecommerce.repository.CartItemRepository;
import com.demo.ecommerce.repository.CartRepository;
import com.demo.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartItemService {
    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    public CartItem addItem(CartItemDTO request) {
        Cart cart = cartRepository.findById(request.getCartId())
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Optional<CartItem> existingItem =
                cartItemRepository.findByCartIdAndProductId(
                        request.getCartId(),
                        request.getProductId());

        if(existingItem.isPresent()) {
            CartItem item = existingItem.get();

            item.setQuantity(item.getQuantity() + request.getQuantity());

            return cartItemRepository.save(item);
        }

        CartItem item = new CartItem();

        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(request.getQuantity());

        return cartItemRepository.save(item);
    }

    public List<CartItem> getAllItems() {
        return cartItemRepository.findAll();
    }

    public String deleteItem(Long id) {
        if(!cartItemRepository.existsById(id)) {
            throw new RuntimeException("Cart Item not found");
        }

        cartItemRepository.deleteById(id);

        return "Item deleted successfully";
    }

    public CartItem updateQuantity(Long id, Integer quantity) {
        CartItem item = cartItemRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Cart Item not found"));

        item.setQuantity(quantity);

        return cartItemRepository.save(item);
    }
}

