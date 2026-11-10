package com.withademm.stores.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.withademm.stores.dto.CategoryResponseDTO;
import com.withademm.stores.dto.ProductRequestDTO;
import com.withademm.stores.dto.ProductResponseDTO;
import com.withademm.stores.exception.GlobalExceptionHandler;
import com.withademm.stores.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Instantiate ObjectMapper directly (no @Autowired needed)
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProductService productService;

    @Test
    @DisplayName("POST /api/products - Should return 201 Created when request is valid")
    void createProduct_ValidInput_Returns201() throws Exception {
        ProductRequestDTO requestDTO = new ProductRequestDTO();
        requestDTO.setName("Mechanical Keyboard");
        requestDTO.setPrice(BigDecimal.valueOf(1200.0));
        requestDTO.setCategoryId(1L);

        CategoryResponseDTO categoryDTO = new CategoryResponseDTO(1L, "Electronics");
        ProductResponseDTO responseDTO = new ProductResponseDTO("prod-1", "Mechanical Keyboard", 1200.0, categoryDTO);

        when(productService.createProduct(any(ProductRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("prod-1"))
                .andExpect(jsonPath("$.name").value("Mechanical Keyboard"))
                .andExpect(jsonPath("$.price").value(1200.0))
                .andExpect(jsonPath("$.category.name").value("Electronics"));
    }

    @Test
    @DisplayName("POST /api/products - Should return 400 Bad Request when validation fails")
    void createProduct_InvalidInput_Returns400() throws Exception {
        ProductRequestDTO invalidRequest = new ProductRequestDTO();
        invalidRequest.setName(""); // Fails @NotBlank
        invalidRequest.setPrice(null); // Fails @NotNull

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("GET /api/products/{id} - Should return 200 OK when product exists")
    void getProductById_Success_Returns200() throws Exception {
        CategoryResponseDTO categoryDTO = new CategoryResponseDTO(1L, "Electronics");
        ProductResponseDTO responseDTO = new ProductResponseDTO("prod-1", "Mechanical Keyboard", 1200.0, categoryDTO);

        when(productService.getProductById("prod-1")).thenReturn(responseDTO);

        mockMvc.perform(get("/api/products/prod-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("prod-1"))
                .andExpect(jsonPath("$.name").value("Mechanical Keyboard"));
    }
}