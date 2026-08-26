package com.demo.ecommerce.service;

import com.demo.ecommerce.dto.ProductDTO;
import com.demo.ecommerce.dto.ProductPageResponseDTO;
import com.demo.ecommerce.dto.ProductResponseDTO;
import com.demo.ecommerce.exception.ResourceNotFoundException;
import com.demo.ecommerce.model.Product;
import com.demo.ecommerce.repository.ProductRepository;
import com.demo.ecommerce.specification.ProductSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class ProductService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("id", "name", "price", "stock", "category");
    private static final int MAX_PAGE_SIZE = 50;
    private static final int DEFAULT_PAGE_SIZE = 8;

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

    public List<String> getCategories() {
        return repository.findDistinctCategories();
    }

    public ProductPageResponseDTO searchProducts(
            String keyword,
            String category,
            Double minPrice,
            Double maxPrice,
            String sortBy,
            String direction,
            Integer page,
            Integer size) {

        int pageNum = (page == null || page < 0) ? 0 : page;
        int pageSize = (size == null || size <= 0) ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);

        if (sortBy == null || sortBy.isBlank() || !ALLOWED_SORT_FIELDS.contains(sortBy)) {
            sortBy = "id";
        }

        Sort.Direction sortDirection;
        if (direction == null || direction.isBlank()) {
            sortDirection = Sort.Direction.ASC;
        } else {
            try {
                sortDirection = Sort.Direction.fromString(direction.toUpperCase());
            } catch (IllegalArgumentException e) {
                sortDirection = Sort.Direction.ASC;
            }
        }

        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by(sortDirection, sortBy));

        Specification<Product> spec = ProductSpecification.buildSearchSpec(keyword, category, minPrice, maxPrice);

        Page<Product> productPage = repository.findAll(spec, pageable);

        List<ProductResponseDTO> content = productPage.getContent().stream()
                .map(this::toResponseDTO)
                .toList();

        ProductPageResponseDTO response = new ProductPageResponseDTO();
        response.setContent(content);
        response.setPage(productPage.getNumber());
        response.setSize(productPage.getSize());
        response.setTotalElements(productPage.getTotalElements());
        response.setTotalPages(productPage.getTotalPages());
        response.setFirst(productPage.isFirst());
        response.setLast(productPage.isLast());

        return response;
    }

    private ProductResponseDTO toResponseDTO(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setDescription(product.getDescription());
        dto.setStock(product.getStock());
        dto.setCategory(product.getCategory());
        dto.setImageUrl(product.getImage_url());
        return dto;
    }
}
