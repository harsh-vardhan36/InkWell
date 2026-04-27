package com.inkWell.post.resource;

import com.inkWell.post.domain.entity.Post;
import com.inkWell.post.repository.PostRepository;
import com.inkWell.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts/admin")
@RequiredArgsConstructor
public class PostAdminResource {

    private final PostRepository postRepository;
    private final PostService postService;

    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        return ResponseEntity.ok(postRepository.findAll());
    }

    @PutMapping("/{id}/feature")
    public ResponseEntity<Post> toggleFeature(@PathVariable Long id, @RequestParam boolean featured) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setFeatured(featured);
        return ResponseEntity.ok(postRepository.save(post));
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<Post> publishPost(@PathVariable Long id) {
        return ResponseEntity.ok(postService.publishPost(id));
    }

    @PutMapping("/{id}/unpublish")
    public ResponseEntity<Post> unpublishPost(@PathVariable Long id) {
        return ResponseEntity.ok(postService.unpublishPost(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deletePost(@PathVariable Long id) {
        postRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Post deleted successfully"));
    }
}
