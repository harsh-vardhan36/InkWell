package com.inkWell.post.service;

import com.inkWell.post.config.RabbitConfig;
import com.inkWell.post.domain.entity.Post;
import com.inkWell.post.domain.enums.PostStatus;
import com.inkWell.post.dto.PostEvent;
import com.inkWell.post.repository.PostRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

/**
 * Service class for managing blog posts.
 * Handles the lifecycle of a post including creation, publication, 
 * updates, and event emission for downstream services.
 */
@Service
public class PostService {

    private final PostRepository postRepository;
    private final RabbitTemplate rabbitTemplate;

    public PostService(PostRepository postRepository, RabbitTemplate rabbitTemplate) {
        this.postRepository = postRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Gets trending posts.
     */
    @Cacheable(value = "trendingPosts")
    public List<Post> getTrendingPosts() {
        return postRepository.findTop10ByStatusOrderByCreatedAtDesc(PostStatus.PUBLISHED);
    }

    /**
     * Gets all published posts.
     */
    @Cacheable(value = "publishedPosts")
    public List<Post> getAllPublishedPosts() {
        return postRepository.findAllByStatus(PostStatus.PUBLISHED);
    }

    /**
     * Gets published posts by category.
     */
    public List<Post> getPostsByCategory(Long categoryId) {
        return postRepository.findAllByCategoryIdAndStatus(categoryId, PostStatus.PUBLISHED);
    }


    /**
     * Creates a new blog post in DRAFT status.
     * Automatically generates a slug and calculates the estimated read time.
     * 
     * @param post The post entity to be created.
     * @return The saved {@link Post} entity.
     */
    public Post createPost(Post post) {
        post.setSlug(generateSlug(post.getTitle()));
        post.setReadTime(calculateReadTime(post.getContent()));
        post.setStatus(PostStatus.DRAFT);
        if (post.getCategoryId() == null) {
            post.setCategoryId(16L); // Default to Uncategorized (ID 16)
        }
        return postRepository.save(post);
    }

    /**
     * Publishes a post by its ID.
     * Sets the status to PUBLISHED, records the publication timestamp, 
     * and emits a RabbitMQ event to notify other services (e.g., newsletter).
     * 
     * @param id The unique identifier of the post to publish.
     * @return The updated {@link Post} entity.
     * @throws RuntimeException if the post is not found.
     */
    @CacheEvict(value = {"trendingPosts", "publishedPosts"}, allEntries = true)
    public Post publishPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setStatus(PostStatus.PUBLISHED);
        post.setPublishedAt(LocalDateTime.now());
        
        Post savedPost = postRepository.save(post);
        
        try {
            // Emit RabbitMQ event for newsletter/notifications
            PostEvent event = PostEvent.builder()
                    .postId(savedPost.getId())
                    .title(savedPost.getTitle())
                    .authorName(savedPost.getAuthorName() != null ? savedPost.getAuthorName() : "InkWell Author")
                    .categoryName("General")
                    .slug(savedPost.getSlug())
                    .build();
            
            rabbitTemplate.convertAndSend(RabbitConfig.POST_EXCHANGE, RabbitConfig.POST_ROUTING_KEY, event);
        } catch (Exception e) {
            // Log the error but don't fail publishing
            System.err.println("Failed to send RabbitMQ event: " + e.getMessage());
        }
        
        return savedPost;
    }

    /**
     * Reverts a published post back to DRAFT status.
     * 
     * @param id The unique identifier of the post to unpublish.
     * @return The updated {@link Post} entity.
     * @throws RuntimeException if the post is not found.
     */
    @CacheEvict(value = {"trendingPosts", "publishedPosts"}, allEntries = true)
    public Post unpublishPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setStatus(PostStatus.DRAFT);
        return postRepository.save(post);
    }

    /**
     * Updates an existing post with new details.
     * Recalculates the slug and read time based on the updated content.
     * 
     * @param id The unique identifier of the post to update.
     * @param postDetails The object containing updated post information.
     * @return The updated {@link Post} entity.
     * @throws RuntimeException if the post is not found.
     */
    @CacheEvict(value = {"trendingPosts", "publishedPosts"}, allEntries = true)
    public Post updatePost(Long id, Post postDetails) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        
        post.setTitle(postDetails.getTitle());
        post.setContent(postDetails.getContent());
        post.setExcerpt(postDetails.getExcerpt());
        post.setFeaturedImageUrl(postDetails.getFeaturedImageUrl());
        post.setCategoryId(postDetails.getCategoryId() != null ? postDetails.getCategoryId() : 16L);
        
        // Recalculate slug and read time
        post.setSlug(generateSlug(post.getTitle()));
        post.setReadTime(calculateReadTime(post.getContent()));
        
        return postRepository.save(post);
    }

    /**
     * Generates a URL-friendly slug from a post title.
     * 
     * @param title The title of the post.
     * @return A lowercased, hyphenated string with a timestamp suffix for uniqueness.
     */
    private String generateSlug(String title) {
        if (title == null) return "";
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .trim()
                .replaceAll("\\s+", "-") + "-" + (System.currentTimeMillis() % 10000);
    }

    /**
     * Calculates the estimated reading time for a post in minutes.
     * Assumes an average reading speed of 200 words per minute.
     * 
     * @param content The full text content of the post.
     * @return Estimated reading time in minutes (minimum 1).
     */
    private int calculateReadTime(String content) {
        if (content == null || content.isEmpty()) return 1;
        int wordsPerMinute = 200;
        int wordCount = content.split("\\s+").length;
        return Math.max(1, (int) Math.ceil((double) wordCount / wordsPerMinute));
    }
}
