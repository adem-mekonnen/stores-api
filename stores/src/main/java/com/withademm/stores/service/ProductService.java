package com.withademm.stores.service;

import com.withademm.stores.dto.*;
import com.withademm.stores.entity.Category;
import com.withademm.stores.entity.Product;
import com.withademm.stores.entity.ProductStatus;
import com.withademm.stores.entity.User;
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
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    // 1. PUBLIC PORTAL: Only returns APPROVED products
    public PagedResponse<ProductResponseDTO> getPublishedProducts(int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Product> productsPage = productRepository.findByStatus(ProductStatus.APPROVED, pageable);
        return toPagedResponse(productsPage);
    }

    // 2. MANAGER QUEUE: Returns PENDING_APPROVAL products awaiting review
    public PagedResponse<ProductResponseDTO> getPendingProducts(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
        Page<Product> productsPage = productRepository.findByStatus(ProductStatus.PENDING_APPROVAL, pageable);
        return toPagedResponse(productsPage);
    }

    // 3. SELLER ACTION: Create a product (starts as PENDING_APPROVAL)
    public ProductResponseDTO createProduct(ProductRequestDTO requestDTO, User seller) {
        Category category = categoryService.findCategoryEntityById(requestDTO.getCategoryId());

        Product product = new Product();
        product.setId(UUID.randomUUID().toString());
        product.setName(requestDTO.getName());
        product.setPrice(requestDTO.getPrice().doubleValue());
        product.setCategory(category);
        product.setStatus(ProductStatus.PENDING_APPROVAL);
        product.setCreatedBy(seller);

        Product saved = productRepository.save(product);
        return convertToDTO(saved);
    }

    // 4. MANAGER/ADMIN ACTION: Approve a product
    public ProductResponseDTO approveProduct(String id, User reviewer) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setStatus(ProductStatus.APPROVED);
        product.setReviewedBy(reviewer);
        product.setRejectionReason(null);

        Product updated = productRepository.save(product);
        return convertToDTO(updated);
    }

    // 5. MANAGER/ADMIN ACTION: Reject a product with feedback reason
    public ProductResponseDTO rejectProduct(String id, RejectProductDTO rejectDTO, User reviewer) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setStatus(ProductStatus.REJECTED);
        product.setReviewedBy(reviewer);
        product.setRejectionReason(rejectDTO.getReason());

        Product updated = productRepository.save(product);
        return convertToDTO(updated);
    }

    // 6. Get Product by ID
    public ProductResponseDTO getProductById(String id) {
        return productRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    // 7. Delete product
    public void deleteProduct(String id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    private PagedResponse<ProductResponseDTO> toPagedResponse(Page<Product> page) {
        List<ProductResponseDTO> content = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    private ProductResponseDTO convertToDTO(Product product) {
        CategoryResponseDTO categoryDTO = null;
        if (product.getCategory() != null) {
            categoryDTO = new CategoryResponseDTO(
                    product.getCategory().getId(),
                    product.getCategory().getName()
            );
        }

        String createdByEmail = product.getCreatedBy() != null ? product.getCreatedBy().getEmail() : null;
        String reviewedByEmail = product.getReviewedBy() != null ? product.getReviewedBy().getEmail() : null;

        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getPrice(),
                categoryDTO,
                product.getStatus(),
                product.getRejectionReason(),
                createdByEmail,
                reviewedByEmail
        );
    }
}