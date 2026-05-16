package com.inkWell.post.resource;

import com.inkWell.post.domain.entity.Post;
import com.inkWell.post.domain.dto.AuthorStatsDTO;
import com.inkWell.post.dto.PostDTO;
import com.inkWell.post.repository.PostRepository;
import com.inkWell.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostResource {

    private final PostRepository postRepository;
    private final PostService postService;

    @GetMapping
    public ResponseEntity<List<PostDTO>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPublishedPosts().stream()
                .map(this::convertToDTO)
                .toList());
    }
    
    @GetMapping("/trending")
    public ResponseEntity<List<PostDTO>> getTrendingPosts() {
        return ResponseEntity.ok(postService.getTrendingPosts().stream()
                .map(this::convertToDTO)
                .toList());
    }

    @GetMapping("/search")
    public ResponseEntity<List<PostDTO>> searchPosts(@RequestParam String query) {
        return ResponseEntity.ok(postService.searchPosts(query).stream()
                .map(this::convertToDTO)
                .toList());
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<PostDTO>> getPostsByAuthor(@PathVariable Long authorId) {
        return ResponseEntity.ok(postRepository.findAllByAuthorId(authorId).stream()
                .map(this::convertToDTO)
                .toList());
    }

    @GetMapping("/author/{authorId}/stats")
    public ResponseEntity<AuthorStatsDTO> getAuthorStats(@PathVariable Long authorId) {
        return ResponseEntity.ok(postService.getAuthorStats(authorId));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<PostDTO>> getPostsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(postService.getPostsByCategory(categoryId).stream()
                .map(this::convertToDTO)
                .toList());
    }


    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPost(@PathVariable Long id) {
        postRepository.incrementViewCount(id);
        return postRepository.findById(id)
                .map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<PostDTO> getPostBySlug(@PathVariable String slug) {
        Optional<Post> post = postRepository.findBySlug(slug);
        post.ifPresent(p -> postRepository.incrementViewCount(p.getId()));
        return post.map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PostDTO> createPost(@RequestBody PostDTO postDTO) {
        Post post = convertToEntity(postDTO);
        return ResponseEntity.ok(convertToDTO(postService.createPost(post)));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Void> likePost(@PathVariable Long id) {
        postRepository.incrementLikeCount(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/unlike")
    public ResponseEntity<Void> unlikePost(@PathVariable Long id) {
        postRepository.decrementLikeCount(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<PostDTO> publishPost(@PathVariable Long id) {
        return ResponseEntity.ok(convertToDTO(postService.publishPost(id)));
    }

    @PutMapping("/{id}/unpublish")
    public ResponseEntity<PostDTO> unpublishPost(@PathVariable Long id) {
        return ResponseEntity.ok(convertToDTO(postService.unpublishPost(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDTO> updatePost(@PathVariable Long id, @RequestBody PostDTO postDTO) {
        Post post = convertToEntity(postDTO);
        return ResponseEntity.ok(convertToDTO(postService.updatePost(id, post)));
    }

    private PostDTO convertToDTO(Post post) {
        return PostDTO.builder()
                .id(post.getId())
                .authorId(post.getAuthorId())
                .authorName(post.getAuthorName())
                .categoryId(post.getCategoryId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .content(post.getContent())
                .excerpt(post.getExcerpt())
                .featuredImageUrl(post.getFeaturedImageUrl())
                .readTime(post.getReadTime())
                .status(post.getStatus())
                .isFeatured(post.isFeatured())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .publishedAt(post.getPublishedAt())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    private Post convertToEntity(PostDTO dto) {
        return Post.builder()
                .authorId(dto.getAuthorId())
                .authorName(dto.getAuthorName())
                .categoryId(dto.getCategoryId())
                .title(dto.getTitle())
                .slug(dto.getSlug())
                .content(dto.getContent())
                .excerpt(dto.getExcerpt())
                .featuredImageUrl(dto.getFeaturedImageUrl())
                .status(dto.getStatus())
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // --- Bookmarks ---

    @PostMapping("/{id}/bookmark")
    public ResponseEntity<Void> bookmarkPost(@PathVariable Long id, @RequestParam Long userId) {
        postService.bookmarkPost(userId, id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/bookmark")
    public ResponseEntity<Void> unbookmarkPost(@PathVariable Long id, @RequestParam Long userId) {
        postService.unbookmarkPost(userId, id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/bookmarked")
    public ResponseEntity<List<PostDTO>> getBookmarkedPosts(@RequestParam Long userId) {
        return ResponseEntity.ok(postService.getBookmarkedPosts(userId).stream()
                .map(this::convertToDTO)
                .toList());
    }

    @GetMapping("/{id}/is-bookmarked")
    public ResponseEntity<Boolean> isBookmarked(@PathVariable Long id, @RequestParam Long userId) {
        return ResponseEntity.ok(postService.isBookmarked(userId, id));
    }
}
