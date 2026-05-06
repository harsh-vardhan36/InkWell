package com.inkWell.category.resource;
import com.inkWell.category.domain.entity.Category;
import com.inkWell.category.domain.entity.Tag;
import com.inkWell.category.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryResource.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void shouldGetAllCategories() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of(new Category()));
        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetCategoryById() throws Exception {
        Category category = new Category();
        category.setId(1L);
        when(categoryService.getCategoryById(1L)).thenReturn(category);
        mockMvc.perform(get("/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldGetAllTags() throws Exception {
        when(categoryService.getAllTags()).thenReturn(List.of(new Tag()));
        mockMvc.perform(get("/categories/tags"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetTrendingTags() throws Exception {
        when(categoryService.getTrendingTags()).thenReturn(List.of(new Tag()));
        mockMvc.perform(get("/categories/tags/trending"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCreateTag() throws Exception {
        Tag tag = new Tag();
        tag.setName("NewTag");
        when(categoryService.createTag(any(Tag.class))).thenReturn(tag);

        mockMvc.perform(post("/categories/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tag)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NewTag"));
    }
}
