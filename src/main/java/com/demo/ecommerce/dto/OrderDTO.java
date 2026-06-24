package com.demo.ecommerce.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrderDTO {
    @NotNull
    private Long userId;
}
