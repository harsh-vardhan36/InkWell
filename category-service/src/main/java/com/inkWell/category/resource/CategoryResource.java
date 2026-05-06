package com.inkWell.category.resource;

import com.inkWell.category.domain.entity.Category;
import com.inkWell.category.domain.entity.Tag;
import com.inkWell.category.dto.CategoryDTO;
import com.inkWell.category.dto.TagDTO;
import com.inkWell.category.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryResource {

    private final CategoryService categoryService;

    public CategoryResource(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories().stream()
                .map(this::convertCategoryToDTO)
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(convertCategoryToDTO(categoryService.getCategoryById(id)));
    }

    // --- Tags ---

    @GetMapping("/tags")
    public ResponseEntity<List<TagDTO>> getAllTags() {
        return ResponseEntity.ok(categoryService.getAllTags().stream()
                .map(this::convertTagToDTO)
                .toList());
    }

    @GetMapping("/tags/trending")
    public ResponseEntity<List<TagDTO>> getTrendingTags() {
        return ResponseEntity.ok(categoryService.getTrendingTags().stream()
                .map(this::convertTagToDTO)
                .toList());
    }

    @PostMapping("/tags")
    public ResponseEntity<TagDTO> createTag(@RequestBody TagDTO tagDTO) {
        Tag tag = Tag.builder()
                .name(tagDTO.getName())
                .slug(tagDTO.getSlug())
                .build();
        return ResponseEntity.ok(convertTagToDTO(categoryService.createTag(tag)));
    }

    private CategoryDTO convertCategoryToDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .parentId(category.getParentId())
                .build();
    }

    private TagDTO convertTagToDTO(Tag tag) {
        return TagDTO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .slug(tag.getSlug())
                .build();
    }

}
