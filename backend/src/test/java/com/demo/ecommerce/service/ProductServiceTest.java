package com.demo.ecommerce.service;

import com.demo.ecommerce.dto.ProductDTO;
import com.demo.ecommerce.exception.ResourceNotFoundException;
import com.demo.ecommerce.model.Product;
import com.demo.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductService productService;

    // --- getProductById ---

    @Test
    void getProductById_existingProduct_returnsProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(29.99);
        product.setStock(10);

        when(repository.findById(1L)).thenReturn(Optional.of(product));

        Product result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Product", result.getName());
        verify(repository).findById(1L);
    }

    @Test
    void getProductById_notFound_throwsResourceNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.getProductById(99L)
        );

        assertTrue(ex.getMessage().contains("99"));
        verify(repository).findById(99L);
    }

    // --- deleteProductById ---

    @Test
    void deleteProductById_notFound_throwsResourceNotFoundException() {
        when(repository.existsById(99L)).thenReturn(false);

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.deleteProductById(99L)
        );

        assertTrue(ex.getMessage().contains("99"));
        verify(repository).existsById(99L);
        verify(repository, never()).deleteById(any());
    }

    @Test
    void deleteProductById_existingProduct_deletesSuccessfully() {
        when(repository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> productService.deleteProductById(1L));

        verify(repository).existsById(1L);
        verify(repository).deleteById(1L);
    }

    // --- updateProduct ---

    @Test
    void updateProduct_notFound_throwsResourceNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ProductDTO dto = new ProductDTO();
        dto.setName("Updated");
        dto.setPrice(10.0);
        dto.setStock(5);
        dto.setCategory("cat");
        dto.setDescription("desc");

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.updateProduct(99L, dto)
        );

        assertTrue(ex.getMessage().contains("99"));
        verify(repository, never()).save(any());
    }

    @Test
    void updateProduct_existingProduct_updatesSuccessfully() {
        Product existing = new Product();
        existing.setId(1L);
        existing.setName("Old");
        existing.setPrice(5.0);
        existing.setStock(1);
        existing.setCategory("old-cat");
        existing.setDescription("old-desc");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductDTO dto = new ProductDTO();
        dto.setName("New");
        dto.setPrice(15.0);
        dto.setStock(20);
        dto.setCategory("new-cat");
        dto.setDescription("new-desc");

        Product result = productService.updateProduct(1L, dto);

        assertEquals("New", result.getName());
        assertEquals(15.0, result.getPrice());
        assertEquals(20, result.getStock());
        verify(repository).save(existing);
    }
}
