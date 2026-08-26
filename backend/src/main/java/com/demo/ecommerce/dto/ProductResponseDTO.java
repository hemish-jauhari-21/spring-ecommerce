package com.demo.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {
    private Long id;
    private String name;
    private Double price;
    private String description;
    private Integer stock;
    private String category;
    @JsonProperty("image_url")
    private String imageUrl;
}