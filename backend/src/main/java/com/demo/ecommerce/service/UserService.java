package com.demo.ecommerce.service;

import com.demo.ecommerce.dto.UserDTO;
import com.demo.ecommerce.dto.UserProfileDTO;
import com.demo.ecommerce.dto.UserUpdateDTO;
import com.demo.ecommerce.exception.DuplicateResourceException;
import com.demo.ecommerce.exception.ResourceNotFoundException;
import com.demo.ecommerce.model.Role;
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
        if (repository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email is already registered");
        }

        User newUser = new User();

        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(Role.USER);

        return repository.save(newUser);
    }

    public List<User> getAllUsers() {
        return repository.findAll();
    }

    public User getUserById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public String deleteById(Long id) {
        Optional<User> user = repository.findById(id);

        if (user.isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }

        repository.deleteById(id);
        return "User with id: " + id + " deleted..";
    }

    public User updateUser(Long id, UserDTO request) {
        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        Optional<User> userWithEmail = repository.findByEmail(request.getEmail());
        if (userWithEmail.isPresent() && !userWithEmail.get().getId().equals(id)) {
            throw new DuplicateResourceException("Email is already registered");
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        return repository.save(user);
    }

    public UserProfileDTO getProfile(User currentUser) {
        UserProfileDTO profile = new UserProfileDTO();
        profile.setId(currentUser.getId());
        profile.setName(currentUser.getName());
        profile.setEmail(currentUser.getEmail());
        profile.setRole(currentUser.getRole().name());
        return profile;
    }

    public User updateProfile(User currentUser, UserUpdateDTO request) {
        Optional<User> userWithEmail = repository.findByEmail(request.getEmail());
        if (userWithEmail.isPresent() && !userWithEmail.get().getId().equals(currentUser.getId())) {
            throw new DuplicateResourceException("Email is already registered");
        }

        currentUser.setName(request.getName());
        currentUser.setEmail(request.getEmail());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            currentUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return repository.save(currentUser);
    }
}
