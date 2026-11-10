package com.withademm.stores.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.withademm.stores.dto.*;
import com.withademm.stores.entity.ProductStatus;
import com.withademm.stores.entity.Role;
import com.withademm.stores.entity.User;
import com.withademm.stores.exception.GlobalExceptionHandler;
import com.withademm.stores.repository.UserRepository;
import com.withademm.stores.service.JwtService;
import com.withademm.stores.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import({GlobalExceptionHandler.class, ProductControllerTest.SecurityTestConfig.class})
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    // --- TEST CONFIG: Tells Spring to resolve @AuthenticationPrincipal as a dummy User instead of trying to bind URL 'id' to User.id ---
    @TestConfiguration
    static class SecurityTestConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(MethodParameter parameter) {
                    return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
                }

                @Override
                public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                              NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                    User testUser = new User("Manager Mike", "manager@stores.com", "pass123", Role.ROLE_MANAGER);
                    testUser.setId(99L);
                    return testUser;
                }
            });
        }
    }

    @Test
    @DisplayName("POST /api/products - Should return 201 Created when request is valid")
    void createProduct_ValidInput_Returns201() throws Exception {
        ProductRequestDTO requestDTO = new ProductRequestDTO();
        requestDTO.setName("Mechanical Keyboard");
        requestDTO.setPrice(BigDecimal.valueOf(1200.0));
        requestDTO.setCategoryId(1L);

        CategoryResponseDTO categoryDTO = new CategoryResponseDTO(1L, "Electronics");
        ProductResponseDTO responseDTO = new ProductResponseDTO("prod-1", "Mechanical Keyboard", 1200.0, categoryDTO);

        when(productService.createProduct(any(ProductRequestDTO.class), any())).thenReturn(responseDTO);

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
        invalidRequest.setName("");
        invalidRequest.setPrice(null);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @DisplayName("PATCH /api/products/{id}/approve - Should return 200 OK when approved")
    void approveProduct_Returns200() throws Exception {
        CategoryResponseDTO categoryDTO = new CategoryResponseDTO(1L, "Electronics");
        ProductResponseDTO approvedDTO = new ProductResponseDTO(
                "prod-1", "Mechanical Keyboard", 1200.0, categoryDTO,
                ProductStatus.APPROVED, null, "seller@stores.com", "manager@stores.com"
        );

        when(productService.approveProduct(eq("prod-1"), any())).thenReturn(approvedDTO);

        mockMvc.perform(patch("/api/products/prod-1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.reviewedByEmail").value("manager@stores.com"));
    }

    @Test
    @DisplayName("PATCH /api/products/{id}/reject - Should return 200 OK when rejected with reason")
    void rejectProduct_Returns200() throws Exception {
        RejectProductDTO rejectDTO = new RejectProductDTO("Photo is not clear");
        CategoryResponseDTO categoryDTO = new CategoryResponseDTO(1L, "Electronics");
        ProductResponseDTO rejectedDTO = new ProductResponseDTO(
                "prod-1", "Mechanical Keyboard", 1200.0, categoryDTO,
                ProductStatus.REJECTED, "Photo is not clear", "seller@stores.com", "manager@stores.com"
        );

        when(productService.rejectProduct(eq("prod-1"), any(RejectProductDTO.class), any())).thenReturn(rejectedDTO);

        mockMvc.perform(patch("/api/products/prod-1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.rejectionReason").value("Photo is not clear"));
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

    @Test
    @DisplayName("GET /api/products - Should return paginated published products")
    void getPublishedProducts_Returns200() throws Exception {
        CategoryResponseDTO categoryDTO = new CategoryResponseDTO(1L, "Electronics");
        ProductResponseDTO item = new ProductResponseDTO("prod-1", "Keyboard", 1200.0, categoryDTO);
        PagedResponse<ProductResponseDTO> pagedResponse = new PagedResponse<>(
                List.of(item), 0, 10, 1L, 1, true
        );

        when(productService.getPublishedProducts(anyInt(), anyInt(), anyString(), anyString())).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/products")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Keyboard"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}