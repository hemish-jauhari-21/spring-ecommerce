package com.demo.ecommerce.service;

import com.demo.ecommerce.dto.*;
import com.demo.ecommerce.exception.BusinessException;
import com.demo.ecommerce.exception.ResourceNotFoundException;
import com.demo.ecommerce.model.*;
import com.demo.ecommerce.repository.*;
import com.demo.ecommerce.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartService cartService;

    private User getCurrentUser(Authentication authentication) {
        return SecurityUtils.getCurrentUser(authentication, userRepository);
    }

    // --------------------------------------------------
    // Place an order from the current user's cart.
    //
    // All-or-nothing: stock validation, order creation,
    // stock reduction and cart clearing run inside a
    // single transaction - any failure rolls everything
    // back (no partial orders, no partial stock loss,
    // cart stays intact).
    //
    // Concurrency: product rows are locked with a native
    // SELECT ... FOR UPDATE scalar projection. A scalar
    // projection is required because entities already in
    // the persistence context would hide concurrent
    // commits; the scalar rows are always fresh. The
    // final decrement is additionally guarded by
    // "stock >= quantity" so overselling is impossible.
    //
    // The total is always calculated server-side from
    // the database product price * quantity. Client
    // totals are never trusted.
    // --------------------------------------------------

    @Transactional
    public OrderResponseDTO placeOrder(Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        // 1. Find user's cart
        Cart cart = cartRepository.findByUserId(currentUser.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cart not found"));

        // 2. Get cart items
        List<CartItem> cartItems =
                cartItemRepository.findByCartId(cart.getId());

        // 3. Check if cart is empty
        if (cartItems.isEmpty()) {
            throw new BusinessException("Cart is empty");
        }

        // 4. Lock all referenced products and read their
        // CURRENT stock/price. Sorted ids keep the lock
        // order deterministic across transactions.
        List<Long> sortedProductIds = cartItems.stream()
                .map(item -> item.getProduct().getId())
                .distinct()
                .sorted()
                .toList();

        Map<Long, ProductRepository.ProductStockRow> lockedRows =
                productRepository.findStockForUpdate(sortedProductIds)
                        .stream()
                        .collect(Collectors.toMap(
                                ProductRepository.ProductStockRow::getId,
                                Function.identity()));

        // 5. Validate stock against the locked (fresh) data
        for (CartItem cartItem : cartItems) {

            ProductRepository.ProductStockRow row =
                    lockedRows.get(cartItem.getProduct().getId());

            int availableStock =
                    row.getStock() != null ? row.getStock() : 0;

            if (cartItem.getQuantity() > availableStock) {

                if (availableStock <= 0) {
                    throw new BusinessException(
                            row.getName() + " is out of stock");
                }

                throw new BusinessException(
                        "Only "
                                + availableStock
                                + " units of "
                                + row.getName()
                                + " are available.");
            }
        }

        // 6. Calculate total amount server-side from DB prices
        double totalAmount = 0;

        for (CartItem cartItem : cartItems) {

            ProductRepository.ProductStockRow row =
                    lockedRows.get(cartItem.getProduct().getId());

            totalAmount +=
                    row.getPrice()
                            * cartItem.getQuantity();
        }

        // 7. Create Order
        Order order = new Order();

        order.setUser(cart.getUser());
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());

        Order savedOrder =
                orderRepository.save(order);

        // 8. Create OrderItems + reduce stock atomically
        for (CartItem cartItem : cartItems) {

            Long productId = cartItem.getProduct().getId();

            Product productReference =
                    productRepository.getReferenceById(productId);

            OrderItem orderItem =
                    new OrderItem();

            orderItem.setOrder(savedOrder);

            orderItem.setProduct(productReference);

            orderItem.setQuantity(
                    cartItem.getQuantity()
            );

            orderItem.setPrice(
                    lockedRows.get(productId).getPrice()
            );

            orderItemRepository.save(orderItem);

            int updatedRows = productRepository.reduceStock(
                    productId,
                    cartItem.getQuantity());

            if (updatedRows != 1) {

                // Defensive: can only happen if stock changed
                // after our validation - roll everything back.
                throw new BusinessException(
                        lockedRows.get(productId).getName()
                                + " has insufficient stock");
            }
        }

        // 9. Clear cart items within the same transaction
        cartItemRepository.deleteAllInBatch(cartItems);

        // 10. Reset authoritative cart total to 0
        cart.setTotalAmount(0.0);
        cartRepository.save(cart);

        // 11. Prepare user response
        User user = savedOrder.getUser();

        UserResponseDTO userResponseDTO =
                new UserResponseDTO(
                        user.getId(),
                        user.getName(),
                        user.getEmail()
                );

        // 12. Return response
        return new OrderResponseDTO(
                savedOrder.getId(),
                userResponseDTO,
                savedOrder.getTotalAmount(),
                savedOrder.getStatus(),
                savedOrder.getCreatedAt()
        );
    }

    public List<OrderResponseDTO> getOrdersByUser(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        List<Order> orders = orderRepository.findByUserId(currentUser.getId());

        return orders.stream()
                .map(this::toOrderResponse)
                .toList();
    }

    public List<OrderResponseDTO> getOrdersByUserId(Long userId,
                                                    Authentication authentication) {
        if (!SecurityUtils.hasRole(authentication, "ROLE_ADMIN")) {
            throw new AccessDeniedException("ADMIN access required");
        }

        List<Order> orders = orderRepository.findByUserId(userId);

        return orders.stream()
                .map(this::toOrderResponse)
                .toList();
    }

    public OrderDetailsResponseDTO getOrderDetails(Long orderId,
                                                   Authentication authentication) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found with id: " + orderId));

        User currentUser = getCurrentUser(authentication);

        boolean isOwner = order.getUser() != null
                && order.getUser().getId().equals(currentUser.getId());

        if (!isOwner
                && !SecurityUtils.hasRole(authentication, "ROLE_ADMIN")) {
            throw new AccessDeniedException("You do not have access to this order");
        }

        User user = order.getUser();

        UserResponseDTO userResponseDTO =
                new UserResponseDTO(
                        user.getId(),
                        user.getName(),
                        user.getEmail()
                );

        List<OrderItem> orderItems =
                orderItemRepository.findByOrderId(orderId);

        List<OrderItemResponseDTO> itemDTOs =
                orderItems.stream()
                        .map(item -> {

                            Product product =
                                    item.getProduct();

                            ProductResponseDTO productDTO =
                                    new ProductResponseDTO(
                                            product.getId(),
                                            product.getName(),
                                            product.getPrice(),
                                            product.getDescription(),
                                            product.getStock(),
                                            product.getCategory(),
                                            product.getImage_url()
                                    );

                            return new OrderItemResponseDTO(
                                    item.getId(),
                                    productDTO,
                                    item.getQuantity(),
                                    item.getPrice()
                            );

                        })
                        .toList();

        return new OrderDetailsResponseDTO(
                order.getId(),
                userResponseDTO,
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                itemDTOs
        );
    }

    private OrderResponseDTO toOrderResponse(Order order) {

        User user = order.getUser();

        UserResponseDTO userResponseDTO =
                new UserResponseDTO(
                        user.getId(),
                        user.getName(),
                        user.getEmail()
                );

        return new OrderResponseDTO(
                order.getId(),
                userResponseDTO,
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}