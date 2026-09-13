package com.demo.ecommerce.controller;

import com.demo.ecommerce.model.Cart;
import com.demo.ecommerce.model.Role;
import com.demo.ecommerce.model.User;
import com.demo.ecommerce.repository.CartRepository;
import com.demo.ecommerce.repository.UserRepository;
import com.demo.ecommerce.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

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
        userA.setName("UserA");
        userA.setEmail("usera_cart@test.com");
        userA.setPassword(passwordEncoder.encode("Pass@123"));
        userA.setRole(Role.USER);
        userRepository.save(userA);
        userAToken = jwtService.generateToken("usera_cart@test.com");

        userB = new User();
        userB.setName("UserB");
        userB.setEmail("userb_cart@test.com");
        userB.setPassword(passwordEncoder.encode("Pass@123"));
        userB.setRole(Role.USER);
        userRepository.save(userB);
        userBToken = jwtService.generateToken("userb_cart@test.com");
    }

    @Test
    void getMyCart_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/ecommerce/cart/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyCart_authenticated_returns200() throws Exception {
        mockMvc.perform(post("/api/v1/ecommerce/cart/add")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/ecommerce/cart/me")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk());
    }

    @Test
    void getCartById_otherUserCart_returns403() throws Exception {
        Cart cart = new Cart();
        cart.setUser(userA);
        cart.setTotalAmount(0.0);
        cart = cartRepository.save(cart);

        mockMvc.perform(get("/api/v1/ecommerce/cart/" + cart.getId())
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllCartItems_normalUser_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/ecommerce/cart-item/all")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isForbidden());
    }
}
