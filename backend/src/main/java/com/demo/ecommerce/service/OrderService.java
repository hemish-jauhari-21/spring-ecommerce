package com.demo.ecommerce.service;

import com.demo.ecommerce.dto.*;
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

    private User getCurrentUser(Authentication authentication) {
        return SecurityUtils.getCurrentUser(authentication, userRepository);
    }

    @Transactional
    public OrderResponseDTO placeOrder(Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        // 1. Find user's cart
        Cart cart = cartRepository.findByUserId(currentUser.getId())
                .orElseThrow(() ->
                        new RuntimeException("Cart not found"));

        // 2. Get cart items
        List<CartItem> cartItems =
                cartItemRepository.findByCartId(cart.getId());

        // 3. Check if cart is empty
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // 4. Check stock BEFORE creating the order
        for (CartItem cartItem : cartItems) {

            Product product = cartItem.getProduct();

            if (product.getStock() < cartItem.getQuantity()) {

                throw new RuntimeException(
                        product.getName()
                                + " has insufficient stock. Available: "
                                + product.getStock()
                );
            }
        }

        // 5. Calculate total amount
        double totalAmount = 0;

        for (CartItem cartItem : cartItems) {

            totalAmount +=
                    cartItem.getProduct().getPrice()
                            * cartItem.getQuantity();
        }

        // 6. Create Order
        Order order = new Order();

        order.setUser(cart.getUser());
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());

        Order savedOrder =
                orderRepository.save(order);

        // 7. Create OrderItems + reduce stock
        for (CartItem cartItem : cartItems) {

            Product product =
                    cartItem.getProduct();

            OrderItem orderItem =
                    new OrderItem();

            orderItem.setOrder(savedOrder);

            orderItem.setProduct(product);

            orderItem.setQuantity(
                    cartItem.getQuantity()
            );

            orderItem.setPrice(
                    product.getPrice()
            );

            orderItemRepository.save(orderItem);

            product.setStock(
                    product.getStock()
                            - cartItem.getQuantity()
            );

            productRepository.save(product);
        }

        // 8. Clear cart
        cartItemRepository.deleteAll(cartItems);

        // 9. Prepare user response
        User user = savedOrder.getUser();

        UserResponseDTO userResponseDTO =
                new UserResponseDTO(
                        user.getId(),
                        user.getName(),
                        user.getEmail()
                );

        // 10. Return response
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
                        new RuntimeException("Order not found"));

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