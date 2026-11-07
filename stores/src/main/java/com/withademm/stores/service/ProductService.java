package com.withademm.stores.service;

import com.withademm.stores.dto.PagedResponse;
import com.withademm.stores.dto.ProductRequestDTO;
import com.withademm.stores.dto.ProductResponseDTO;
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

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // 1. Updated getAllProducts with Pagination and Sorting
    public PagedResponse<ProductResponseDTO> getAllProducts(
            int pageNo,
            int pageSize,
            String sortBy,
            String sortDir) {

        // Check sort direction: ascending or descending
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        // Create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        // Fetch paginated products from database
        Page<Product> productsPage = productRepository.findAll(pageable);

        // Convert Product entities to ProductResponseDTO list
        List<ProductResponseDTO> content = productsPage.getContent()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // Return custom response with metadata
        return new PagedResponse<>(
                content,
                productsPage.getNumber(),
                productsPage.getSize(),
                productsPage.getTotalElements(),
                productsPage.getTotalPages(),
                productsPage.isLast()
        );
    }

    // 2. Create product
    public ProductResponseDTO createProduct(ProductRequestDTO requestDTO) {
        Product product = new Product();
        product.setId(UUID.randomUUID().toString());
        product.setName(requestDTO.getName());
        product.setPrice(requestDTO.getPrice().doubleValue());

        Product saved = productRepository.save(product);
        return convertToDTO(saved);
    }

    // 3. Get product by ID
    public ProductResponseDTO getProductById(String id) {
        return productRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    // 4. Update product
    public ProductResponseDTO updateProduct(String id, ProductRequestDTO requestDTO) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    existingProduct.setName(requestDTO.getName());
                    existingProduct.setPrice(requestDTO.getPrice().doubleValue());
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

    private ProductResponseDTO convertToDTO(Product product) {
        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getPrice()
        );
    }
}