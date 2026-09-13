package com.demo.ecommerce.controller;

import com.demo.ecommerce.model.Order;
import com.demo.ecommerce.model.OrderStatus;
import com.demo.ecommerce.model.Role;
import com.demo.ecommerce.model.User;
import com.demo.ecommerce.repository.OrderRepository;
import com.demo.ecommerce.repository.UserRepository;
import com.demo.ecommerce.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User userA;
    private String userAToken;
    private User userB;
    private String userBToken;

    @BeforeEach
    void setUp() {
        userA = new User();
        userA.setName("OrderUserA");
        userA.setEmail("ordera@test.com");
        userA.setPassword(passwordEncoder.encode("Pass@123"));
        userA.setRole(Role.USER);
        userRepository.save(userA);
        userAToken = jwtService.generateToken("ordera@test.com");

        userB = new User();
        userB.setName("OrderUserB");
        userB.setEmail("orderb@test.com");
        userB.setPassword(passwordEncoder.encode("Pass@123"));
        userB.setRole(Role.USER);
        userRepository.save(userB);
        userBToken = jwtService.generateToken("orderb@test.com");
    }

    @Test
    void getMyOrders_authenticated_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/ecommerce/order/me")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk());
    }

    @Test
    void getMyOrders_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/ecommerce/order/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getOrderDetails_otherUserOrder_returns403() throws Exception {
        Order order = new Order();
        order.setUser(userA);
        order.setTotalAmount(50.0);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        mockMvc.perform(get("/api/v1/ecommerce/order/" + order.getId())
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllOrders_normalUser_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/ecommerce/order/all")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isForbidden());
    }
}
