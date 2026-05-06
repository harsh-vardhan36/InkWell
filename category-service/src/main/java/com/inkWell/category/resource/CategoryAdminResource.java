package com.inkWell.category.resource;

import com.inkWell.category.domain.entity.Category;
import com.inkWell.category.domain.entity.Tag;
import com.inkWell.category.repository.CategoryRepository;
import com.inkWell.category.repository.TagRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/categories/admin")
public class CategoryAdminResource {

    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final com.inkWell.category.service.CategoryService categoryService;

    public CategoryAdminResource(CategoryRepository categoryRepository, TagRepository tagRepository, com.inkWell.category.service.CategoryService categoryService) {
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        return ResponseEntity.ok(categoryService.createCategory(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category categoryDetails) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());
        category.setParentId(categoryDetails.getParentId());
        // Service will handle slug generation if needed, or we do it here
        return ResponseEntity.ok(categoryRepository.save(category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(Map.of("message", "Category deleted successfully"));
    }

    // Tags
    @GetMapping("/tags")
    public ResponseEntity<List<Tag>> getAllTags() {
        return ResponseEntity.ok(tagRepository.findAllByOrderByUsageCountDesc());
    }

    @PostMapping("/tags")
    public ResponseEntity<Tag> createTag(@RequestBody Tag tag) {
        return ResponseEntity.ok(tagRepository.save(tag));
    }

    @DeleteMapping("/tags/{id}")
    public ResponseEntity<Map<String, String>> deleteTag(@PathVariable Long id) {
        tagRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Tag deleted successfully"));
    }
}
