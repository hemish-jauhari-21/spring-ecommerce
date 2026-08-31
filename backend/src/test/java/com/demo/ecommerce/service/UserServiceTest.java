package com.demo.ecommerce.service;

import com.demo.ecommerce.dto.UserDTO;
import com.demo.ecommerce.exception.DuplicateResourceException;
import com.demo.ecommerce.exception.ResourceNotFoundException;
import com.demo.ecommerce.model.Role;
import com.demo.ecommerce.model.User;
import com.demo.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserDTO buildValidDTO() {
        UserDTO dto = new UserDTO();
        dto.setName("John");
        dto.setEmail("john@example.com");
        dto.setPassword("Pass@123");
        return dto;
    }

    // --- saveUser ---

    @Test
    void saveUser_newUser_assignsUserRole() {
        when(repository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Pass@123")).thenReturn("encoded-pass");
        when(repository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        User result = userService.saveUser(buildValidDTO());

        assertNotNull(result);
        assertEquals(Role.USER, result.getRole());
        assertEquals("encoded-pass", result.getPassword());
        verify(repository).save(any(User.class));
    }

    @Test
    void saveUser_duplicateEmail_throwsDuplicateResourceException() {
        User existing = new User();
        existing.setId(1L);
        existing.setEmail("john@example.com");
        when(repository.findByEmail("john@example.com")).thenReturn(Optional.of(existing));

        assertThrows(
                DuplicateResourceException.class,
                () -> userService.saveUser(buildValidDTO())
        );

        verify(repository, never()).save(any());
    }

    // --- getUserById ---

    @Test
    void getUserById_notFound_throwsResourceNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserById(99L)
        );
    }

    @Test
    void getUserById_existingUser_returnsUser() {
        User user = new User();
        user.setId(1L);
        user.setName("John");
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUserById(1L);

        assertEquals("John", result.getName());
    }

    // --- deleteById ---

    @Test
    void deleteById_notFound_throwsResourceNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.deleteById(99L)
        );

        verify(repository, never()).deleteById(any());
    }

    // --- updateUser ---

    @Test
    void updateUser_notFound_throwsResourceNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.updateUser(99L, buildValidDTO())
        );
    }

    @Test
    void updateUser_duplicateEmailByOtherUser_throwsDuplicateResourceException() {
        User existing = new User();
        existing.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setEmail("john@example.com");
        when(repository.findByEmail("john@example.com")).thenReturn(Optional.of(otherUser));

        assertThrows(
                DuplicateResourceException.class,
                () -> userService.updateUser(1L, buildValidDTO())
        );
    }
}
