package com.demo.ecommerce.service;

import com.demo.ecommerce.dto.*;
import com.demo.ecommerce.model.*;
import com.demo.ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    public OrderResponseDTO createOrder(OrderDTO request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = new Order();

        order.setUser(user);
        order.setStatus("PENDING");
        order.setTotalAmount(0.0);
        order.setCreatedAt(LocalDateTime.now());

        Order saveOrder = orderRepository.save(order);

        UserResponseDTO userResponseDTO = new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail()
        );

        return new OrderResponseDTO(
                saveOrder.getId(),
                userResponseDTO,
                saveOrder.getTotalAmount(),
                saveOrder.getStatus(),
                saveOrder.getCreatedAt()
        );
    }

    @Transactional
    public OrderResponseDTO placeOrder(PlaceOrderDTO request) {

        // 1. Find user's cart
        Cart cart = cartRepository.findByUserId(request.getUserId())
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


            // Create OrderItem
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


            // Reduce stock
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

    public List<OrderResponseDTO> getOrdersByUser(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);

        return orders.stream()
                .map(order -> {

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

                })
                .toList();
    }

    public OrderDetailsResponseDTO getOrderDetails(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));


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
}
