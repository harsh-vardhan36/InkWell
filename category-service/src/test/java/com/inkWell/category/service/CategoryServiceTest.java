package com.inkWell.category.service;

import com.inkWell.category.domain.entity.Category;
import com.inkWell.category.domain.entity.Tag;
import com.inkWell.category.repository.CategoryRepository;
import com.inkWell.category.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;
    private Tag testTag;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1L)
                .name("Technology")
                .build();

        testTag = Tag.builder()
                .id(1L)
                .name("Java")
                .build();
    }

    @Test
    void getAllCategories_Success() {
        when(categoryRepository.findAll()).thenReturn(Arrays.asList(testCategory));
        List<Category> result = categoryService.getAllCategories();
        assertEquals(1, result.size());
    }

    @Test
    void getCategoryById_Success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        Category result = categoryService.getCategoryById(1L);
        assertEquals("Technology", result.getName());
    }

    @Test
    void getCategoryById_NotFound_ThrowsException() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> categoryService.getCategoryById(1L));
    }

    @Test
    void createCategory_Success() {
        when(categoryRepository.existsByName("Technology")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        
        Category result = categoryService.createCategory(testCategory);
        assertNotNull(result);
        assertEquals("technology", result.getSlug());
    }

    @Test
    void createCategory_AlreadyExists_ThrowsException() {
        when(categoryRepository.existsByName("Technology")).thenReturn(true);
        assertThrows(RuntimeException.class, () -> categoryService.createCategory(testCategory));
    }

    @Test
    void deleteCategory_Success() {
        categoryService.deleteCategory(1L);
        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void getAllTags_Success() {
        when(tagRepository.findAll()).thenReturn(Arrays.asList(testTag));
        assertFalse(categoryService.getAllTags().isEmpty());
    }

    @Test
    void getTrendingTags_Success() {
        when(tagRepository.findAllByOrderByUsageCountDesc()).thenReturn(Arrays.asList(testTag));
        assertFalse(categoryService.getTrendingTags().isEmpty());
    }

    @Test
    void createTag_Success() {
        when(tagRepository.save(any(Tag.class))).thenReturn(testTag);
        Tag result = categoryService.createTag(testTag);
        assertEquals("java", result.getSlug());
    }

    @Test
    void testBuilders_Coverage() {
        Category category = Category.builder()
                .id(2L)
                .name("Test")
                .slug("test")
                .build();
        assertEquals(2L, category.getId());
        assertEquals("Test", category.getName());
        assertEquals("test", category.getSlug());

        Tag tag = Tag.builder()
                .id(2L)
                .name("TestTag")
                .slug("test-tag")
                .usageCount(10)
                .build();
        assertEquals(2L, tag.getId());
        assertEquals("TestTag", tag.getName());
        assertEquals("test-tag", tag.getSlug());
        assertEquals(10, tag.getUsageCount());
    }
}
