package com.demo.ecommerce.service;

import com.demo.ecommerce.dto.UserDTO;
import com.demo.ecommerce.model.User;
import com.demo.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User saveUser(UserDTO request) {
        User newUser = new User();

        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(request.getRole());

        return repository.save(newUser);
    }

    public List<User> getAllUsers() {
        return repository.findAll();
    }

    public User getUserById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public String deleteById(Long id) {
        Optional<User> user = repository.findById(id);

        if (user.isEmpty()) {
            return "User not present..";
        }

        repository.deleteById(id);
        return "User with id: " + id + " deleted..";
    }

    public User updateUser(Long id, UserDTO request) {
        User user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        return repository.save(user);
    }
}
