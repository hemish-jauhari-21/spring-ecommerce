package com.demo.ecommerce.service;

import com.demo.ecommerce.dto.CartItemDTO;
import com.demo.ecommerce.dto.CartItemResponseDTO;
import com.demo.ecommerce.dto.ProductResponseDTO;
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

    private CartItemResponseDTO convertToDTO(CartItem item) {
        Product product = item.getProduct();

        ProductResponseDTO productDTO = new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                product.getStock(),
                product.getCategory(),
                product.getImage_url()
        );

        return new CartItemResponseDTO(
                item.getId(),
                productDTO,
                item.getQuantity()
        );
    }

    public CartItemResponseDTO addItem(CartItemDTO request) {
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

            CartItem savedItem = cartItemRepository.save(item);

            return convertToDTO(savedItem);
        }

        CartItem item = new CartItem();

        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(request.getQuantity());

        CartItem savedItem = cartItemRepository.save(item);
        return convertToDTO(savedItem);
    }

    public List<CartItemResponseDTO> getAllItems() {
        return cartItemRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public String deleteItem(Long id) {
        if(!cartItemRepository.existsById(id)) {
            throw new RuntimeException("Cart Item not found");
        }

        cartItemRepository.deleteById(id);

        return "Item deleted successfully";
    }

    public CartItemResponseDTO updateQuantity(Long id, Integer quantity) {
        CartItem item = cartItemRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Cart Item not found"));

        item.setQuantity(quantity);

        CartItem savedItem = cartItemRepository.save(item);

        return convertToDTO(savedItem);
    }
}

