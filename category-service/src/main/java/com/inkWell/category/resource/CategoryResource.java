package com.inkWell.category.resource;

import com.inkWell.category.domain.entity.Category;
import com.inkWell.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryResource {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    // --- Tags ---

    @GetMapping("/tags")
    public ResponseEntity<List<com.inkWell.category.domain.entity.Tag>> getAllTags() {
        return ResponseEntity.ok(categoryService.getAllTags());
    }

    @GetMapping("/tags/trending")
    public ResponseEntity<List<com.inkWell.category.domain.entity.Tag>> getTrendingTags() {
        return ResponseEntity.ok(categoryService.getTrendingTags());
    }

    @PostMapping("/tags")
    public ResponseEntity<com.inkWell.category.domain.entity.Tag> createTag(@RequestBody com.inkWell.category.domain.entity.Tag tag) {
        return ResponseEntity.ok(categoryService.createTag(tag));
    }

}
