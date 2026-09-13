package com.demo.ecommerce.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String BASE = "/api/v1/ecommerce/auth";

    @Test
    void register_validInput_returns200WithUser() throws Exception {
        String body = """
                {
                    "name": "Alice",
                    "email": "alice@test.com",
                    "password": "Pass@123"
                }
                """;

        mockMvc.perform(post(BASE + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@test.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        String body = """
                {
                    "name": "Bob",
                    "email": "bob@test.com",
                    "password": "Pass@123"
                }
                """;

        mockMvc.perform(post(BASE + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(post(BASE + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void login_validCredentials_returns200WithToken() throws Exception {
        String regBody = """
                {
                    "name": "Carol",
                    "email": "carol@test.com",
                    "password": "Pass@123"
                }
                """;

        mockMvc.perform(post(BASE + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(regBody))
                .andExpect(status().isOk());

        String loginBody = """
                {
                    "email": "carol@test.com",
                    "password": "Pass@123"
                }
                """;

        mockMvc.perform(post(BASE + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("carol@test.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void login_invalidCredentials_returns401() throws Exception {
        String regBody = """
                {
                    "name": "Dave",
                    "email": "dave@test.com",
                    "password": "Pass@123"
                }
                """;

        mockMvc.perform(post(BASE + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(regBody))
                .andExpect(status().isOk());

        String loginBody = """
                {
                    "email": "dave@test.com",
                    "password": "WrongPass@1"
                }
                """;

        mockMvc.perform(post(BASE + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isUnauthorized());
    }
}
