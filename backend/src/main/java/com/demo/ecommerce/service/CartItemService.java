package com.demo.ecommerce.service;

import com.demo.ecommerce.dto.CartItemDTO;
import com.demo.ecommerce.dto.CartItemResponseDTO;
import com.demo.ecommerce.dto.ProductResponseDTO;
import com.demo.ecommerce.model.Cart;
import com.demo.ecommerce.model.CartItem;
import com.demo.ecommerce.model.Product;
import com.demo.ecommerce.model.User;
import com.demo.ecommerce.repository.CartItemRepository;
import com.demo.ecommerce.repository.CartRepository;
import com.demo.ecommerce.repository.ProductRepository;
import com.demo.ecommerce.repository.UserRepository;
import com.demo.ecommerce.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartService cartService;

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

    private User getCurrentUser(Authentication authentication) {
        return SecurityUtils.getCurrentUser(authentication, userRepository);
    }

    private boolean isOwner(CartItem item, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        return item.getCart() != null
                && item.getCart().getUser() != null
                && item.getCart().getUser().getId().equals(currentUser.getId());
    }

    private void checkOwnership(CartItem item, Authentication authentication) {
        if (!isOwner(item, authentication)
                && !SecurityUtils.hasRole(authentication, "ROLE_ADMIN")) {
            throw new AccessDeniedException("You do not have access to this cart item");
        }
    }

    public CartItemResponseDTO addItem(CartItemDTO request, Authentication authentication) {
        Cart cart = cartService.getOrCreateCart(authentication);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Optional<CartItem> existingItem =
                cartItemRepository.findByCartIdAndProductId(
                        cart.getId(),
                        request.getProductId());

        if (existingItem.isPresent()) {
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

    public List<CartItem> getMyItems(Authentication authentication) {
        Cart cart = cartService.getOrCreateCart(authentication);

        return cartItemRepository.findByCartId(cart.getId());
    }

    public List<CartItem> getItemsByCartId(Long cartId, Authentication authentication) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        User currentUser = getCurrentUser(authentication);

        if (!cart.getUser().getId().equals(currentUser.getId())
                && !SecurityUtils.hasRole(authentication, "ROLE_ADMIN")) {
            throw new AccessDeniedException("You do not have access to this cart");
        }

        return cartItemRepository.findByCartId(cartId);
    }

    public String deleteItem(Long id, Authentication authentication) {
        CartItem item = cartItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cart Item not found"));

        checkOwnership(item, authentication);

        cartItemRepository.deleteById(id);

        return "Item deleted successfully";
    }

    public CartItemResponseDTO updateQuantity(Long id, Integer quantity,
                                              Authentication authentication) {
        CartItem item = cartItemRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Cart Item not found"));

        checkOwnership(item, authentication);

        item.setQuantity(quantity);

        CartItem savedItem = cartItemRepository.save(item);

        return convertToDTO(savedItem);
    }
}