package com.withademm.stores.service;

import com.withademm.stores.dto.CategoryResponseDTO;
import com.withademm.stores.dto.ProductRequestDTO;
import com.withademm.stores.dto.ProductResponseDTO;
import com.withademm.stores.entity.Category;
import com.withademm.stores.entity.Product;
import com.withademm.stores.exception.ResourceNotFoundException;
import com.withademm.stores.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    // 1. Create fake dependencies (Mocks)
    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryService categoryService;

    // 2. Inject the fake dependencies into the real ProductService
    @InjectMocks
    private ProductService productService;

    private Category testCategory;
    private Product testProduct;

    // Runs before each @Test to prepare fresh sample data
    @BeforeEach
    void setUp() {
        testCategory = new Category(1L, "Electronics");
        testProduct = new Product("prod-123", "Gaming Mouse", 450.0, testCategory);
    }

    @Test
    @DisplayName("Should successfully create a product when category exists")
    void createProduct_Success() {
        // ARRANGE (Given)
        ProductRequestDTO requestDTO = new ProductRequestDTO();
        requestDTO.setName("Gaming Mouse");
        requestDTO.setPrice(BigDecimal.valueOf(450.0));
        requestDTO.setCategoryId(1L);

        // Tell mock CategoryService: when categoryId 1 is searched, return testCategory
        when(categoryService.findCategoryEntityById(1L)).thenReturn(testCategory);

        // Tell mock ProductRepository: when any product is saved, return testProduct
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // ACT (When)
        ProductResponseDTO result = productService.createProduct(requestDTO);

        // ASSERT (Then)
        assertNotNull(result);
        assertEquals("Gaming Mouse", result.getName());
        assertEquals(450.0, result.getPrice());
        assertNotNull(result.getCategory());
        assertEquals("Electronics", result.getCategory().getName());

        // Verify that the repository's save method was actually called exactly 1 time
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should return product when a valid ID is provided")
    void getProductById_Success() {
        // ARRANGE
        when(productRepository.findById("prod-123")).thenReturn(Optional.of(testProduct));

        // ACT
        ProductResponseDTO result = productService.getProductById("prod-123");

        // ASSERT
        assertNotNull(result);
        assertEquals("prod-123", result.getId());
        assertEquals("Gaming Mouse", result.getName());
        verify(productRepository, times(1)).findById("prod-123");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product ID does not exist")
    void getProductById_NotFound_ThrowsException() {
        // ARRANGE: Mock repository returns empty Optional (product not found)
        when(productRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // ACT & ASSERT: Expect ResourceNotFoundException to be thrown
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.getProductById("invalid-id")
        );

        // Verify the exception message matches
        assertEquals("Product not found with id: invalid-id", exception.getMessage());

        // Verify findById was called
        verify(productRepository, times(1)).findById("invalid-id");
    }
}