package com.demo.ecommerce.service;

import com.demo.ecommerce.dto.CartItemDTO;
import com.demo.ecommerce.dto.CartItemResponseDTO;
import com.demo.ecommerce.dto.ProductResponseDTO;
import com.demo.ecommerce.exception.BusinessException;
import com.demo.ecommerce.exception.ResourceNotFoundException;
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
import org.springframework.transaction.annotation.Transactional;

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
    // Stock validation used by ADD and UPDATE.
    // Backend is authoritative - the frontend check
    // is UX only and can always be bypassed.
    // --------------------------------------------------

    private void validateStockAgainstProduct(
            Product product,
            int requestedQuantity) {

        int availableStock =
                product.getStock() != null
                        ? product.getStock()
                        : 0;

        if (requestedQuantity > availableStock) {

            if (availableStock <= 0) {
                throw new BusinessException(
                        product.getName() + " is out of stock"
                );
            }

            throw new BusinessException(
                    "Only "
                            + availableStock
                            + " units of "
                            + product.getName()
                            + " are available."
            );
        }
    }


    // --------------------------------------------------
    // Add product to current user's cart
    // --------------------------------------------------

    @Transactional
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
                        new ResourceNotFoundException(
                                "Product not found with id: " + request.getProductId()
                        )
                );


        // Service-level quantity validation
        // (in addition to the @Min(1) bean validation)
        if (request.getQuantity() == null
                || request.getQuantity() < 1) {

            throw new BusinessException(
                    "Quantity must be at least 1"
            );
        }


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

            // Stock validation:
            // resulting quantity must fit in stock
            validateStockAgainstProduct(
                    product,
                    newQuantity
            );

            item.setQuantity(newQuantity);

            CartItem savedItem =
                    cartItemRepository.save(item);


            // Server recalculates the authoritative total
            cartService.recalculateTotal(cart);

            return convertToDTO(savedItem);
        }


        // New cart item
        validateStockAgainstProduct(
                product,
                request.getQuantity()
        );


        CartItem item = new CartItem();

        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(request.getQuantity());


        CartItem savedItem =
                cartItemRepository.save(item);


        // Server recalculates the authoritative total
        cartService.recalculateTotal(cart);

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
                        new ResourceNotFoundException(
                                "Cart not found with id: " + cartId
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

    @Transactional
    public String deleteItem(
            Long id,
            Authentication authentication) {

        CartItem item =
                cartItemRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Cart item not found with id: " + id
                                )
                        );


        // Ownership check
        checkOwnership(
                item,
                authentication
        );


        Cart cart = item.getCart();


        cartItemRepository.deleteById(id);


        // Server recalculates the authoritative total
        // so no stale totals remain after deletion
        if (cart != null) {
            cartService.recalculateTotal(cart);
        }

        return "Item deleted successfully";
    }


    // --------------------------------------------------
    // Update cart item quantity
    // USER -> own item
    // ADMIN -> any item
    // --------------------------------------------------

    @Transactional
    public CartItemResponseDTO updateQuantity(
            Long id,
            Integer quantity,
            Authentication authentication) {

        // Quantity validation
        if (quantity == null || quantity < 1) {

            throw new BusinessException(
                    "Quantity must be at least 1"
            );
        }


        CartItem item =
                cartItemRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Cart item not found with id: " + id
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


        // Stock validation:
        // requested quantity must fit within current stock
        validateStockAgainstProduct(
                product,
                quantity
        );


        item.setQuantity(quantity);


        CartItem savedItem =
                cartItemRepository.save(item);


        // Server recalculates the authoritative total
        if (item.getCart() != null) {
            cartService.recalculateTotal(item.getCart());
        }

        return convertToDTO(savedItem);
    }
}
