package com.demo.ecommerce.repository;

import com.demo.ecommerce.dto.ProductDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<ProductDTO, Integer> {
}
