package com.inkWell.admin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "category-service", path = "/categories/admin")
public interface CategoryServiceClient {

    @GetMapping
    List<Object> getAllCategories();

    @PostMapping
    Object createCategory(@RequestBody Object category);

    @PutMapping("/{id}")
    Map<String, String> updateCategory(@PathVariable("id") Long id, @RequestBody Object category);

    @DeleteMapping("/{id}")
    Map<String, String> deleteCategory(@PathVariable("id") Long id);

    @GetMapping("/tags")
    List<Object> getAllTags();

    @PostMapping("/tags")
    Object createTag(@RequestBody Object tag);

    @DeleteMapping("/tags/{id}")
    Map<String, String> deleteTag(@PathVariable("id") Long id);
}
