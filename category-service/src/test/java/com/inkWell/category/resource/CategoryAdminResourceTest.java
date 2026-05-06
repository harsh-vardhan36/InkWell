package com.inkWell.category.resource;
import com.inkWell.category.domain.entity.Category;
import com.inkWell.category.domain.entity.Tag;
import com.inkWell.category.service.CategoryService;
import com.inkWell.category.repository.CategoryRepository;
import com.inkWell.category.repository.TagRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryAdminResource.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryAdminResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryRepository categoryRepository;

    @MockitoBean
    private TagRepository tagRepository;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void shouldGetAllCategories() throws Exception {
        when(categoryRepository.findAll()).thenReturn(List.of(new Category()));
        mockMvc.perform(get("/categories/admin"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCreateCategory() throws Exception {
        Category category = new Category();
        category.setName("Tech");
        when(categoryService.createCategory(any(Category.class))).thenReturn(category);

        mockMvc.perform(post("/categories/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tech"));
    }

    @Test
    void shouldUpdateCategory() throws Exception {
        Category category = new Category();
        category.setName("Updated");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        mockMvc.perform(put("/categories/admin/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(category)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteCategory() throws Exception {
        mockMvc.perform(delete("/categories/admin/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Category deleted successfully"));
    }

    @Test
    void shouldGetAllTags() throws Exception {
        when(tagRepository.findAllByOrderByUsageCountDesc()).thenReturn(List.of(new Tag()));
        mockMvc.perform(get("/categories/admin/tags"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCreateTag() throws Exception {
        Tag tag = new Tag();
        tag.setName("NewTag");
        when(tagRepository.save(any(Tag.class))).thenReturn(tag);

        mockMvc.perform(post("/categories/admin/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tag)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NewTag"));
    }

    @Test
    void shouldDeleteTag() throws Exception {
        mockMvc.perform(delete("/categories/admin/tags/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tag deleted successfully"));
    }
}
