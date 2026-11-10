
package com.withademm.stores.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

public class ProductRequestDTO {

    @NotBlank(message = "Product name is required")
    @Size(
            min = 3,
            max = 50,
            message = "Product name must be between 3 and 50 characters"
    )
    private String name;

    @NotNull(message = "Product price is required")
    @DecimalMin(
            value = "0.0",
            inclusive = false,
            message = "Product price must be greater than zero"
    )
    @Digits(
            integer = 10,
            fraction = 2,
            message = "Product price must have at most 10 integer digits and 2 decimal places"
    )
    private BigDecimal price;
    @NotNull(message = "Category ID is required")
    private Long categoryId;
    // Default constructor
    public ProductRequestDTO() {
    }

    // Parameterized constructor
    public ProductRequestDTO(String name, BigDecimal price) {
        this.name = name;
        this.price = price;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    // Add getter and setter:
    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}
