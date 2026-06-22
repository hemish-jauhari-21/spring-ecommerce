package com.demo.ecommerce.dto;

import com.demo.ecommerce.model.User;
import jakarta.persistence.OneToOne;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CartDTO {
    private Long id;
}
