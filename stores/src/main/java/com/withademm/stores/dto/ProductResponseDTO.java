package com.withademm.stores.dto;

import com.withademm.stores.entity.ProductStatus;

public class ProductResponseDTO {

    private String id;
    private String name;
    private double price;
    private CategoryResponseDTO category;
    private ProductStatus status;
    private String rejectionReason;
    private String createdByEmail;
    private String reviewedByEmail;

    public ProductResponseDTO(String id, String name, double price, CategoryResponseDTO category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.status = ProductStatus.PENDING_APPROVAL;
    }

    // 2. KEEP THIS 8-parameter constructor (for full workflow):
    public ProductResponseDTO(String id, String name, double price, CategoryResponseDTO category,
                              ProductStatus status, String rejectionReason,
                              String createdByEmail, String reviewedByEmail) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.status = status;
        this.rejectionReason = rejectionReason;
        this.createdByEmail = createdByEmail;
        this.reviewedByEmail = reviewedByEmail;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public CategoryResponseDTO getCategory() {
        return category;
    }

    public void setCategory(CategoryResponseDTO category) {
        this.category = category;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getCreatedByEmail() {
        return createdByEmail;
    }

    public void setCreatedByEmail(String createdByEmail) {
        this.createdByEmail = createdByEmail;
    }

    public String getReviewedByEmail() {
        return reviewedByEmail;
    }

    public void setReviewedByEmail(String reviewedByEmail) {
        this.reviewedByEmail = reviewedByEmail;
    }
}