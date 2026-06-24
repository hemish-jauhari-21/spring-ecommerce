package com.demo.ecommerce.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CartResponseDTO {
    private Long id;
    private Double totalAmount;
    private UserResponseDTO user;
}
