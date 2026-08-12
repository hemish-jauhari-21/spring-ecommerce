package com.demo.ecommerce.controller;

import com.demo.ecommerce.dto.OrderDetailsResponseDTO;
import com.demo.ecommerce.dto.OrderResponseDTO;
import com.demo.ecommerce.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ecommerce/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/place")
    public OrderResponseDTO placeOrder(Authentication authentication) {
        return orderService.placeOrder(authentication);
    }

    @GetMapping("/me")
    public List<OrderResponseDTO> getMyOrders(Authentication authentication) {
        return orderService.getOrdersByUser(authentication);
    }

    @GetMapping("/user/{userId}")
    public List<OrderResponseDTO> getOrdersByUser(@PathVariable Long userId,
                                                  Authentication authentication) {
        return orderService.getOrdersByUserId(userId, authentication);
    }

    @GetMapping("/{orderId}")
    public OrderDetailsResponseDTO getOrderDetails(@PathVariable Long orderId,
                                                   Authentication authentication) {
        return orderService.getOrderDetails(orderId, authentication);
    }
}