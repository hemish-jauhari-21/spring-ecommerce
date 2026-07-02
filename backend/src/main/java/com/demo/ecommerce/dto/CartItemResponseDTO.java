    package com.demo.ecommerce.dto;

    import lombok.*;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public class CartItemResponseDTO {
        private Long id;
        private ProductResponseDTO product;
        private Integer quantity;
    }
