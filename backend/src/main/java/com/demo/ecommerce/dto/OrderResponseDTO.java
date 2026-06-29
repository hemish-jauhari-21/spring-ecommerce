package com.demo.ecommerce.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrderResponseDTO {
    private Long id;
    private UserResponseDTO user;
    private Double totalAmount;
    private String status;
    private LocalDateTime createdAt;
}