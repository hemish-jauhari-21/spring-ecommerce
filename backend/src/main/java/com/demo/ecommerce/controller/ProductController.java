package com.demo.ecommerce.controller;

import com.demo.ecommerce.dto.ProductDTO;
import com.demo.ecommerce.dto.ProductPageResponseDTO;
import com.demo.ecommerce.model.Product;
import com.demo.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ecommerce/products")
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
    public Product getProductById(@PathVariable Long id) {
        return service.getProductById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteProductById(@PathVariable Long id) {
        service.deleteProductById(id);
    }

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDTO request) {
        return service.updateProduct(id, request);
    }

    @GetMapping("/search")
    public ProductPageResponseDTO searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return service.searchProducts(keyword, category, minPrice, maxPrice, sortBy, direction, page, size);
    }

    @GetMapping("/categories")
    public List<String> getCategories() {
        return service.getCategories();
    }
}
