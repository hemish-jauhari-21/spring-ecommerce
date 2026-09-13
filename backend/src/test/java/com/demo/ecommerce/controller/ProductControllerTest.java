package com.demo.ecommerce.controller;

import com.demo.ecommerce.model.Product;
import com.demo.ecommerce.model.Role;
import com.demo.ecommerce.model.User;
import com.demo.ecommerce.repository.ProductRepository;
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
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User adminUser;
    private String adminToken;
    private User normalUser;
    private String normalUserToken;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setName("Admin");
        adminUser.setEmail("admin_prod@test.com");
        adminUser.setPassword(passwordEncoder.encode("Pass@123"));
        adminUser.setRole(Role.ADMIN);
        userRepository.save(adminUser);
        adminToken = jwtService.generateToken("admin_prod@test.com");

        normalUser = new User();
        normalUser.setName("Normal");
        normalUser.setEmail("normal_prod@test.com");
        normalUser.setPassword(passwordEncoder.encode("Pass@123"));
        normalUser.setRole(Role.USER);
        userRepository.save(normalUser);
        normalUserToken = jwtService.generateToken("normal_prod@test.com");
    }

    @Test
    void getProduct_existingId_returns200() throws Exception {
        Product product = new Product();
        product.setName("Laptop");
        product.setPrice(999.99);
        product.setDescription("A powerful laptop");
        product.setStock(10);
        product.setCategory("Electronics");
        product = productRepository.save(product);

        mockMvc.perform(get("/api/v1/ecommerce/products/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(999.99));
    }

    @Test
    void getProduct_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/ecommerce/products/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addProduct_normalUser_returns403() throws Exception {
        String body = """
                {
                    "name": "Phone",
                    "price": 499.99,
                    "description": "A smartphone",
                    "stock": 25,
                    "category": "Electronics"
                }
                """;

        mockMvc.perform(post("/api/v1/ecommerce/products/add")
                        .header("Authorization", "Bearer " + normalUserToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }
}
