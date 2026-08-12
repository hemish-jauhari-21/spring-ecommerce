package com.demo.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO {
    private Long id;

    @NotBlank(message = "Username cannot be blank !!")
    private String name;

    @Email(message = "Not a valid email address !!")
    @NotBlank(message = "Username cannot be blank !!")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 4, max = 10, message = "Password must be between 4 and 10 characters")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$",
            message = "Password must contain at least one digit, lowercase, uppercase, and special character"
    )
    private String password;
}
