package com.withademm.stores.controller;

import com.withademm.stores.dto.PagedResponse;
import com.withademm.stores.dto.ProductRequestDTO;
import com.withademm.stores.dto.ProductResponseDTO;
import com.withademm.stores.dto.RejectProductDTO;
import com.withademm.stores.entity.User;
import com.withademm.stores.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // 1. PUBLIC CUSTOMER PORTAL: Anyone can view ONLY approved products
    // GET /api/products?pageNo=0&pageSize=10&sortBy=price&sortDir=asc
    @GetMapping
    public ResponseEntity<PagedResponse<ProductResponseDTO>> getPublishedProducts(
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "name", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc", required = false) String sortDir
    ) {
        return ResponseEntity.ok(productService.getPublishedProducts(pageNo, pageSize, sortBy, sortDir));
    }

    // 2. MANAGER/ADMIN REVIEW QUEUE: View pending products awaiting approval
    // GET /api/products/pending?pageNo=0&pageSize=10
    @GetMapping("/pending")
    public ResponseEntity<PagedResponse<ProductResponseDTO>> getPendingProducts(
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize
    ) {
        return ResponseEntity.ok(productService.getPendingProducts(pageNo, pageSize));
    }

    // 3. SELLER ACTION: Upload product (starts in PENDING_APPROVAL, tracks creator)
    // POST /api/products
    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(
            @Valid @RequestBody ProductRequestDTO requestDTO,
            @AuthenticationPrincipal User currentUser
    ) {
        return new ResponseEntity<>(productService.createProduct(requestDTO, currentUser), HttpStatus.CREATED);
    }

    // 4. MANAGER/ADMIN ACTION: Approve a product and publish it to the portal
    // PATCH /api/products/{id}/approve
    @PatchMapping("/{id}/approve")
    public ResponseEntity<ProductResponseDTO> approveProduct(
            @PathVariable String id,
            @AuthenticationPrincipal User reviewer
    ) {
        return ResponseEntity.ok(productService.approveProduct(id, reviewer));
    }

    // 5. MANAGER/ADMIN ACTION: Reject a product with feedback reason
    // PATCH /api/products/{id}/reject
    @PatchMapping("/{id}/reject")
    public ResponseEntity<ProductResponseDTO> rejectProduct(
            @PathVariable String id,
            @Valid @RequestBody RejectProductDTO rejectDTO,
            @AuthenticationPrincipal User reviewer
    ) {
        return ResponseEntity.ok(productService.rejectProduct(id, rejectDTO, reviewer));
    }

    // 6. VIEW SINGLE PRODUCT: Get details by ID
    // GET /api/products/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable String id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // 7. ADMIN ACTION: Delete a product
    // DELETE /api/products/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}