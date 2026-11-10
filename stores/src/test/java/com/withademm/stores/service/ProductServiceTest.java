package com.withademm.stores.service;

import com.withademm.stores.dto.ProductRequestDTO;
import com.withademm.stores.dto.ProductResponseDTO;
import com.withademm.stores.dto.RejectProductDTO;
import com.withademm.stores.entity.Category;
import com.withademm.stores.entity.Product;
import com.withademm.stores.entity.ProductStatus;
import com.withademm.stores.entity.Role;
import com.withademm.stores.entity.User;
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

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private ProductService productService;

    private Category testCategory;
    private Product testProduct;
    private User testSeller;
    private User testReviewer;

    @BeforeEach
    void setUp() {
        testCategory = new Category(1L, "Electronics");

        // 1. Mock Seller (who creates the product)
        testSeller = new User("John Seller", "seller@stores.com", "pass123", Role.ROLE_SELLER);
        testSeller.setId(10L);

        // 2. Mock Reviewer (Manager who approves/rejects)
        testReviewer = new User("Mike Manager", "manager@stores.com", "pass123", Role.ROLE_MANAGER);
        testReviewer.setId(20L);

        // 3. Mock Product (starts in PENDING_APPROVAL)
        testProduct = new Product("prod-123", "Gaming Mouse", 450.0, testCategory, testSeller);
        testProduct.setStatus(ProductStatus.PENDING_APPROVAL);
    }

    @Test
    @DisplayName("Create Product - Should set status to PENDING_APPROVAL and track seller")
    void createProduct_Success() {
        ProductRequestDTO requestDTO = new ProductRequestDTO();
        requestDTO.setName("Gaming Mouse");
        requestDTO.setPrice(BigDecimal.valueOf(450.0));
        requestDTO.setCategoryId(1L);

        when(categoryService.findCategoryEntityById(1L)).thenReturn(testCategory);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Passed both requestDTO and testSeller:
        ProductResponseDTO result = productService.createProduct(requestDTO, testSeller);

        assertNotNull(result);
        assertEquals("Gaming Mouse", result.getName());
        assertEquals(450.0, result.getPrice());
        assertEquals(ProductStatus.PENDING_APPROVAL, result.getStatus());
        assertEquals("seller@stores.com", result.getCreatedByEmail());

        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Approve Product - Should change status to APPROVED and set reviewer")
    void approveProduct_Success() {
        when(productRepository.findById("prod-123")).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponseDTO result = productService.approveProduct("prod-123", testReviewer);

        assertNotNull(result);
        assertEquals(ProductStatus.APPROVED, result.getStatus());
        assertEquals("manager@stores.com", result.getReviewedByEmail());
        assertNull(result.getRejectionReason());

        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    @DisplayName("Reject Product - Should change status to REJECTED and record feedback reason")
    void rejectProduct_Success() {
        RejectProductDTO rejectDTO = new RejectProductDTO("Photo is not clear");
        when(productRepository.findById("prod-123")).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponseDTO result = productService.rejectProduct("prod-123", rejectDTO, testReviewer);

        assertNotNull(result);
        assertEquals(ProductStatus.REJECTED, result.getStatus());
        assertEquals("Photo is not clear", result.getRejectionReason());
        assertEquals("manager@stores.com", result.getReviewedByEmail());

        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    @DisplayName("Approve Product - Should throw ResourceNotFoundException when product does not exist")
    void approveProduct_NotFound_ThrowsException() {
        when(productRepository.findById("invalid-id")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.approveProduct("invalid-id", testReviewer)
        );

        assertEquals("Product not found with id: invalid-id", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Get Product by ID - Should return product when valid ID is provided")
    void getProductById_Success() {
        when(productRepository.findById("prod-123")).thenReturn(Optional.of(testProduct));

        ProductResponseDTO result = productService.getProductById("prod-123");

        assertNotNull(result);
        assertEquals("prod-123", result.getId());
        assertEquals("Gaming Mouse", result.getName());
        verify(productRepository, times(1)).findById("prod-123");
    }

    @Test
    @DisplayName("Get Product by ID - Should throw ResourceNotFoundException when ID does not exist")
    void getProductById_NotFound_ThrowsException() {
        when(productRepository.findById("invalid-id")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.getProductById("invalid-id")
        );

        assertEquals("Product not found with id: invalid-id", exception.getMessage());
        verify(productRepository, times(1)).findById("invalid-id");
    }
}