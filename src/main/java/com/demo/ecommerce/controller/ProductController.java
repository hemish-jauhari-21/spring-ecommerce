package com.demo.ecommerce.controller;

import com.demo.ecommerce.dto.ProductDTO;
import com.demo.ecommerce.model.Product;
import com.demo.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/ecommerce/product")
public class ProductController {
    @Autowired
    private ProductService service;

    @PostMapping("/add")
    public Product saveProduct(@Valid @RequestBody ProductDTO request) {
        return service.saveProduct(request);
    }

    @GetMapping("/all")
    public List<Product> getAllProducts() {
        return service.getAllProducts();
    }

    @GetMapping("/{id}")
    public Optional<Product> getProductById(@PathVariable Long id) {
        return service.getProductById(id);
    }
}
