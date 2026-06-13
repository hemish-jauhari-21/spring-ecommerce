package com.demo.ecommerce.service;

import com.demo.ecommerce.dto.UserDTO;
import com.demo.ecommerce.model.User;
import com.demo.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository repository;

    public User saveUser(UserDTO request) {
        User newUser = new User();

        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());

        return repository.save(newUser);
    }
}
