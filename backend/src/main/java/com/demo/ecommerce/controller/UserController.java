package com.demo.ecommerce.controller;

import com.demo.ecommerce.dto.UserDTO;
import com.demo.ecommerce.dto.UserProfileDTO;
import com.demo.ecommerce.dto.UserUpdateDTO;
import com.demo.ecommerce.model.User;
import com.demo.ecommerce.repository.UserRepository;
import com.demo.ecommerce.security.SecurityUtils;
import com.demo.ecommerce.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ecommerce/user")
public class UserController {
    @Autowired
    private UserService service;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/me")
    public UserProfileDTO getMyProfile(Authentication authentication) {
        User user = SecurityUtils.getCurrentUser(authentication, userRepository);
        return service.getProfile(user);
    }

    @PutMapping("/me")
    public User updateMyProfile(Authentication authentication,
                                @Valid @RequestBody UserUpdateDTO request) {
        User user = SecurityUtils.getCurrentUser(authentication, userRepository);
        return service.updateProfile(user, request);
    }

    @PostMapping("/add")
    public User createUser(@Valid @RequestBody UserDTO request) {
        return service.saveUser(request);
    }

    @GetMapping("/all")
    public List<User> getAll() {
        return service.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return service.getUserById(id);
    }

    @DeleteMapping("/delete/{id}")
    public String deleteById(@PathVariable Long id) {
        return service.deleteById(id);
    }

    @PutMapping("/update/{id}")
    public User updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO request) {
        return service.updateUser(id, request);
    }
}
