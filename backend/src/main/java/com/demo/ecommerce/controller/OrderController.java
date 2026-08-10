package com.demo.ecommerce.controller;

import com.demo.ecommerce.dto.OrderDTO;
import com.demo.ecommerce.dto.OrderDetailsResponseDTO;
import com.demo.ecommerce.dto.OrderResponseDTO;
import com.demo.ecommerce.dto.PlaceOrderDTO;
import com.demo.ecommerce.model.Order;
import com.demo.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ecommerce/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/place")
    public OrderResponseDTO placeOrder(@Valid @RequestBody PlaceOrderDTO request) {
        return orderService.placeOrder(request);
    }

    @GetMapping("/user/{userId}")
    public List<OrderResponseDTO> getOrdersByUser(
            @PathVariable Long userId) {

        return orderService.getOrdersByUser(userId);
    }

    @GetMapping("/{orderId}")
    public OrderDetailsResponseDTO getOrderDetails(
            @PathVariable Long orderId) {

        return orderService.getOrderDetails(orderId);
    }
}
