package com.demo.ecommerce.service;

import com.demo.ecommerce.dto.ProductDTO;
import com.demo.ecommerce.model.Product;
import com.demo.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    @Autowired
    private ProductRepository repository;

    public Product saveProduct(ProductDTO request) {
        Product newProduct = new Product();

        newProduct.setName(request.getName());
        newProduct.setPrice(request.getPrice());
        newProduct.setDescription(request.getDescription());
        newProduct.setStock(request.getStock());
        newProduct.setCategory(request.getCategory());
        newProduct.setImage_url(request.getImage_url());

        return repository.save(newProduct);
    }

    public List<Product> getAllProducts() {
        return repository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return repository.findById(id);
    }
}
