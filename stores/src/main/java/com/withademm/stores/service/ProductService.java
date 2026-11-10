package com.withademm.stores.service;

import com.withademm.stores.dto.CategoryResponseDTO;
import com.withademm.stores.dto.PagedResponse;
import com.withademm.stores.dto.ProductRequestDTO;
import com.withademm.stores.dto.ProductResponseDTO;
import com.withademm.stores.entity.Category;
import com.withademm.stores.entity.Product;
import com.withademm.stores.exception.ResourceNotFoundException;
import com.withademm.stores.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService; // 1. Injected CategoryService

    public ProductService(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    // 1. Get All Products with Pagination and Sorting
    public PagedResponse<ProductResponseDTO> getAllProducts(
            int pageNo,
            int pageSize,
            String sortBy,
            String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Product> productsPage = productRepository.findAll(pageable);

        List<ProductResponseDTO> content = productsPage.getContent()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                productsPage.getNumber(),
                productsPage.getSize(),
                productsPage.getTotalElements(),
                productsPage.getTotalPages(),
                productsPage.isLast()
        );
    }

    // 2. Create product with Category
    public ProductResponseDTO createProduct(ProductRequestDTO requestDTO) {
        // Step A: Fetch Category by ID (throws ResourceNotFoundException automatically if ID doesn't exist)
        Category category = categoryService.findCategoryEntityById(requestDTO.getCategoryId());

        // Step B: Build Product Entity
        Product product = new Product();
        product.setId(UUID.randomUUID().toString());
        product.setName(requestDTO.getName());
        product.setPrice(requestDTO.getPrice().doubleValue());
        product.setCategory(category); // Step C: Link Relationship

        Product saved = productRepository.save(product);
        return convertToDTO(saved);
    }

    // 3. Get product by ID
    public ProductResponseDTO getProductById(String id) {
        return productRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    // 4. Update product with Category
    public ProductResponseDTO updateProduct(String id, ProductRequestDTO requestDTO) {
        // Step A: Fetch Category by ID to ensure it exists
        Category category = categoryService.findCategoryEntityById(requestDTO.getCategoryId());

        return productRepository.findById(id)
                .map(existingProduct -> {
                    existingProduct.setName(requestDTO.getName());
                    existingProduct.setPrice(requestDTO.getPrice().doubleValue());
                    existingProduct.setCategory(category); // Step B: Update Category link

                    Product updated = productRepository.save(existingProduct);
                    return convertToDTO(updated);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    // 5. Delete product
    public void deleteProduct(String id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    // Helper: Convert Entity to Response DTO with nested Category
    private ProductResponseDTO convertToDTO(Product product) {
        CategoryResponseDTO categoryDTO = null;

        // If category is present, convert it to CategoryResponseDTO
        if (product.getCategory() != null) {
            categoryDTO = new CategoryResponseDTO(
                    product.getCategory().getId(),
                    product.getCategory().getName()
            );
        }

        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getPrice(),
                categoryDTO // nested category DTO passed here
        );
    }
}