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
        // Find user's cart
        Cart cart = cartRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        // Get all items from the cart
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

        // Check if cart is empty
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Calculate total Amount
        double totalAmount = 0;

        for (CartItem item: cartItems) {
            totalAmount += item.getProduct().getPrice() * item.getQuantity();
        }

        // Create new order
        Order order = new Order();

        order.setUser(cart.getUser());
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        // Create OrderItems
        for (CartItem cartItem: cartItems) {

            // Get product
            Product product = cartItem.getProduct();

            // Check stock
            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException(
                        product.getName() + "is out of stock"
                );
            }

            // Create OrderItem
            OrderItem orderItem = new OrderItem();

            orderItem.setOrder(savedOrder);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProduct().getPrice());

            orderItemRepository.save(orderItem);

            // Reduce stock
            product.setStock(product.getStock() - cartItem.getQuantity());

            // Save updated product
            productRepository.save(product);
        }

        // Clear Cart
        cartItemRepository.deleteAll(cartItems);

        UserResponseDTO userResponseDTO = new UserResponseDTO(
                savedOrder.getUser().getId(),
                savedOrder.getUser().getName(),
                savedOrder.getUser().getEmail()
        );

        return new OrderResponseDTO(
                savedOrder.getId(),
                userResponseDTO,
                savedOrder.getTotalAmount(),
                savedOrder.getStatus(),
                savedOrder.getCreatedAt()
        );
    }
}
