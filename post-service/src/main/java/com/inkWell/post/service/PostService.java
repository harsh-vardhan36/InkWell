package com.inkWell.post.service;

import com.inkWell.post.config.RabbitConfig;
import com.inkWell.post.domain.entity.Post;
import com.inkWell.post.domain.enums.PostStatus;
import com.inkWell.post.dto.PostEvent;
import com.inkWell.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final RabbitTemplate rabbitTemplate;

    public Post createPost(Post post) {
        post.setSlug(generateSlug(post.getTitle()));
        post.setReadTime(calculateReadTime(post.getContent()));
        post.setStatus(PostStatus.DRAFT);
        return postRepository.save(post);
    }

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
                    .build();
            
            rabbitTemplate.convertAndSend(RabbitConfig.POST_EXCHANGE, RabbitConfig.POST_ROUTING_KEY, event);
        } catch (Exception e) {
            // Log the error but don't fail publishing
            System.err.println("Failed to send RabbitMQ event: " + e.getMessage());
        }
        
        return savedPost;
    }

    public Post unpublishPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setStatus(PostStatus.DRAFT);
        return postRepository.save(post);
    }

    public Post updatePost(Long id, Post postDetails) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        
        post.setTitle(postDetails.getTitle());
        post.setContent(postDetails.getContent());
        post.setExcerpt(postDetails.getExcerpt());
        post.setFeaturedImageUrl(postDetails.getFeaturedImageUrl());
        
        // Recalculate slug and read time
        post.setSlug(generateSlug(post.getTitle()));
        post.setReadTime(calculateReadTime(post.getContent()));
        
        return postRepository.save(post);
    }

    private String generateSlug(String title) {
        if (title == null) return "";
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .trim()
                .replaceAll("\\s+", "-") + "-" + (System.currentTimeMillis() % 10000);
    }

    private int calculateReadTime(String content) {
        if (content == null || content.isEmpty()) return 1;
        int wordsPerMinute = 200;
        int wordCount = content.split("\\s+").length;
        return Math.max(1, (int) Math.ceil((double) wordCount / wordsPerMinute));
    }
}
