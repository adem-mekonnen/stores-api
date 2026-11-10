package com.withademm.stores.dto;

public class ProductResponseDTO {

    private String id;
    private String name;
    private double price;
    private CategoryResponseDTO category;

    public ProductResponseDTO() {
    }

    public ProductResponseDTO(String id, String name, double price, CategoryResponseDTO category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
    }

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
}