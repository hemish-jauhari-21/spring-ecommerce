package com.demo.ecommerce.controller;

import com.demo.ecommerce.dto.UserDTO;
import com.demo.ecommerce.model.User;
import com.demo.ecommerce.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ecommerce/user")
public class UserController {
    @Autowired
    private UserService service;

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
