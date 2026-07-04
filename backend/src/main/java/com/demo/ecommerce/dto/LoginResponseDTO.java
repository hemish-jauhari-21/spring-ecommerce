package com.demo.ecommerce.dto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LoginResponseDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String token;
}
