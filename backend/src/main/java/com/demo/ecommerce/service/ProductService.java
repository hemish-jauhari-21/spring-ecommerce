package com.demo.ecommerce.service;

import com.demo.ecommerce.dto.ProductDTO;
import com.demo.ecommerce.exception.ResourceNotFoundException;
import com.demo.ecommerce.model.Product;
import com.demo.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public Product getProductById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    public void deleteProductById(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        repository.deleteById(id);
    }

    public Product updateProduct(Long id, ProductDTO request) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setStock(request.getStock());
        product.setImage_url(request.getImage_url());

        return repository.save(product);
    }
}
