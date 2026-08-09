package com.demo.ecommerce.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PlaceOrderDTO {
    @NotNull
    private Long userId;
}
