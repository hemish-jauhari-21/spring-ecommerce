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


    // --------------------------------------------------
    // Convert CartItem entity -> CartItemResponseDTO
    // --------------------------------------------------

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


    // --------------------------------------------------
    // Get currently logged-in user
    // --------------------------------------------------

    private User getCurrentUser(
            Authentication authentication) {

        return SecurityUtils.getCurrentUser(
                authentication,
                userRepository
        );
    }


    // --------------------------------------------------
    // Check whether cart item belongs to current user
    // --------------------------------------------------

    private boolean isOwner(
            CartItem item,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        return item.getCart() != null
                && item.getCart().getUser() != null
                && item.getCart()
                .getUser()
                .getId()
                .equals(currentUser.getId());
    }


    // --------------------------------------------------
    // Check ownership
    // USER -> own item only
    // ADMIN -> any item
    // --------------------------------------------------

    private void checkOwnership(
            CartItem item,
            Authentication authentication) {

        boolean owner = isOwner(
                item,
                authentication
        );

        boolean admin = SecurityUtils.hasRole(
                authentication,
                "ROLE_ADMIN"
        );

        if (!owner && !admin) {

            throw new AccessDeniedException(
                    "You do not have access to this cart item"
            );
        }
    }


    // --------------------------------------------------
    // Add product to current user's cart
    // --------------------------------------------------

    public CartItemResponseDTO addItem(
            CartItemDTO request,
            Authentication authentication) {

        // Get current user's cart.
        // CartService automatically creates one
        // if the user does not have a cart.
        Cart cart = cartService.getOrCreateCart(
                authentication
        );

        // Find product
        Product product = productRepository
                .findById(request.getProductId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Product not found"
                        )
                );


        // Check if product already exists in cart
        Optional<CartItem> existingItem =
                cartItemRepository.findByCartIdAndProductId(
                        cart.getId(),
                        request.getProductId()
                );


        // If product already exists,
        // increase quantity
        if (existingItem.isPresent()) {

            CartItem item = existingItem.get();

            int newQuantity =
                    item.getQuantity()
                            + request.getQuantity();

            // Stock validation
            if (newQuantity > product.getStock()) {

                throw new RuntimeException(
                        "Requested quantity exceeds available stock"
                );
            }

            item.setQuantity(newQuantity);

            CartItem savedItem =
                    cartItemRepository.save(item);

            return convertToDTO(savedItem);
        }


        // New cart item
        if (request.getQuantity() > product.getStock()) {

            throw new RuntimeException(
                    "Requested quantity exceeds available stock"
            );
        }


        CartItem item = new CartItem();

        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(request.getQuantity());


        CartItem savedItem =
                cartItemRepository.save(item);

        return convertToDTO(savedItem);
    }


    // --------------------------------------------------
    // Get ALL cart items
    // SecurityConfig should make this ADMIN-only
    // --------------------------------------------------

    public List<CartItemResponseDTO> getAllItems() {

        return cartItemRepository
                .findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }


    // --------------------------------------------------
    // Get current user's cart items
    // --------------------------------------------------

    public List<CartItemResponseDTO> getMyItems(
            Authentication authentication) {

        Cart cart = cartService.getOrCreateCart(
                authentication
        );

        return cartItemRepository
                .findByCartId(cart.getId())
                .stream()
                .map(this::convertToDTO)
                .toList();
    }


    // --------------------------------------------------
    // Get items from a specific cart
    // USER -> own cart
    // ADMIN -> any cart
    // --------------------------------------------------

    public List<CartItemResponseDTO> getItemsByCartId(
            Long cartId,
            Authentication authentication) {

        Cart cart = cartRepository
                .findById(cartId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Cart not found"
                        )
                );

        User currentUser =
                getCurrentUser(authentication);


        boolean owner =
                cart.getUser() != null
                        && cart.getUser()
                        .getId()
                        .equals(currentUser.getId());

        boolean admin =
                SecurityUtils.hasRole(
                        authentication,
                        "ROLE_ADMIN"
                );


        if (!owner && !admin) {

            throw new AccessDeniedException(
                    "You do not have access to this cart"
            );
        }


        return cartItemRepository
                .findByCartId(cartId)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }


    // --------------------------------------------------
    // Delete cart item
    // USER -> own item
    // ADMIN -> any item
    // --------------------------------------------------

    public String deleteItem(
            Long id,
            Authentication authentication) {

        CartItem item =
                cartItemRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Cart Item not found"
                                )
                        );


        // Ownership check
        checkOwnership(
                item,
                authentication
        );


        cartItemRepository.deleteById(id);

        return "Item deleted successfully";
    }


    // --------------------------------------------------
    // Update cart item quantity
    // USER -> own item
    // ADMIN -> any item
    // --------------------------------------------------

    public CartItemResponseDTO updateQuantity(
            Long id,
            Integer quantity,
            Authentication authentication) {

        // Quantity validation
        if (quantity == null || quantity < 1) {

            throw new IllegalArgumentException(
                    "Quantity must be at least 1"
            );
        }


        CartItem item =
                cartItemRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Cart Item not found"
                                )
                        );


        // Ownership check
        checkOwnership(
                item,
                authentication
        );


        // Get product
        Product product =
                item.getProduct();


        // Stock validation
        if (quantity > product.getStock()) {

            throw new RuntimeException(
                    "Requested quantity exceeds available stock"
            );
        }


        item.setQuantity(quantity);


        CartItem savedItem =
                cartItemRepository.save(item);


        return convertToDTO(savedItem);
    }
}