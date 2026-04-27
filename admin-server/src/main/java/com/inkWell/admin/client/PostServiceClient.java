package com.inkWell.admin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "post-service", path = "/posts/admin")
public interface PostServiceClient {

    @GetMapping
    List<Object> getAllPosts();

    @PutMapping("/{id}/feature")
    Map<String, String> featurePost(@PathVariable("id") Long id, @RequestParam("featured") boolean featured);

    @DeleteMapping("/{id}")
    Map<String, String> deletePost(@PathVariable("id") Long id);
    
    @GetMapping("/stats")
    Map<String, Object> getPostStats();
}
