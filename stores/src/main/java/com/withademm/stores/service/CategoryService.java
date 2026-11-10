package com.withademm.stores.service;

import com.withademm.stores.dto.CategoryRequestDTO;
import com.withademm.stores.dto.CategoryResponseDTO;
import com.withademm.stores.entity.Category;
import com.withademm.stores.exception.ResourceNotFoundException;
import com.withademm.stores.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponseDTO createCategory(CategoryRequestDTO requestDTO) {
        Category category = new Category();
        category.setName(requestDTO.getName());
        Category saved = categoryRepository.save(category);
        return new CategoryResponseDTO(saved.getId(), saved.getName());
    }

    public List<CategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(cat -> new CategoryResponseDTO(cat.getId(), cat.getName()))
                .collect(Collectors.toList());
    }

    public Category findCategoryEntityById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }
}