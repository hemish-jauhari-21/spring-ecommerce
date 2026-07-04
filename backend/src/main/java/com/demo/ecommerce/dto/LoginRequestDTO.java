package com.demo.ecommerce.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LoginRequestDTO {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
