package com.demo.ecommerce.service;

import com.demo.ecommerce.dto.OrderDTO;
import com.demo.ecommerce.dto.OrderResponseDTO;
import com.demo.ecommerce.dto.UserResponseDTO;
import com.demo.ecommerce.model.Order;
import com.demo.ecommerce.model.User;
import com.demo.ecommerce.repository.OrderRepository;
import com.demo.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

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
}
