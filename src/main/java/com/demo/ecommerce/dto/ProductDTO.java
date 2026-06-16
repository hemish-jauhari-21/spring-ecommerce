package com.demo.ecommerce.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductDTO {
    private Long id;

    @NotBlank(message = "Product name cannot be blank !!")
    private String name;

    private Double price;
    private String description;
    private Integer stock;
    private String category;

    @NotBlank(message = "Image can't be displayed")
    private String image_url;
}
