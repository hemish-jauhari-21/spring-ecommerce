package com.demo.ecommerce.controller;

import com.demo.ecommerce.dto.OrderDTO;
import com.demo.ecommerce.model.Order;
import com.demo.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ecommerce/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/place")
    public Order createOrder(@Valid @RequestBody OrderDTO request) {
        return orderService.createOrder(request);
    }
}
