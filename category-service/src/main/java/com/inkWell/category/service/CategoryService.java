package com.inkWell.category.service;

import com.inkWell.category.domain.entity.Category;
import com.inkWell.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new RuntimeException("Category name already exists");
        }
        category.setSlug(generateSlug(category.getName()));
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    // --- Tag Methods ---
    private final com.inkWell.category.repository.TagRepository tagRepository;

    public List<com.inkWell.category.domain.entity.Tag> getAllTags() {
        return tagRepository.findAll();
    }

    public List<com.inkWell.category.domain.entity.Tag> getTrendingTags() {
        return tagRepository.findAllByOrderByUsageCountDesc();
    }

    public com.inkWell.category.domain.entity.Tag createTag(com.inkWell.category.domain.entity.Tag tag) {
        tag.setSlug(generateSlug(tag.getName()));
        return tagRepository.save(tag);
    }

    private String generateSlug(String name) {
        if (name == null) return "";
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .trim()
                .replaceAll("\\s+", "-");
    }
}
