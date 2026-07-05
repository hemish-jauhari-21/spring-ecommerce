package com.demo.ecommerce.controller;

import com.demo.ecommerce.dto.LoginRequestDTO;
import com.demo.ecommerce.dto.LoginResponseDTO;
import com.demo.ecommerce.dto.UserDTO;
import com.demo.ecommerce.model.User;
import com.demo.ecommerce.service.AuthService;
import com.demo.ecommerce.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ecommerce/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public LoginResponseDTO login(
            @Valid @RequestBody LoginRequestDTO request) {
        return authService.login(request);

    }

    @PostMapping("/register")
    public User register (@Valid @RequestBody UserDTO request) {
        return userService.saveUser(request);
    }
}
