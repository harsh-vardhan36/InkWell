package com.inkWell.admin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "comment-service", path = "/comments/admin")
public interface CommentServiceClient {

    @GetMapping
    List<Object> getAllComments();

    @PutMapping("/{id}/status")
    Map<String, String> updateCommentStatus(@PathVariable("id") Long id, @RequestParam("status") String status);

    @DeleteMapping("/{id}")
    Map<String, String> deleteComment(@PathVariable("id") Long id);
}
