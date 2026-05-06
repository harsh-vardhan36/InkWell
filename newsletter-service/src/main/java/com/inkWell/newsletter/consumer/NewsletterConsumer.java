package com.inkWell.newsletter.consumer;

import com.inkWell.newsletter.config.RabbitConfig;
import com.inkWell.newsletter.dto.PostEvent;
import com.inkWell.newsletter.service.NewsletterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ Consumer for the newsletter service.
 * Listens for post-published events and triggers automated newsletter campaigns.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NewsletterConsumer {

    private final NewsletterService newsletterService;

    /**
     * Consumes messages from the post-published queue.
     * Extracts post details and invokes the newsletter notification logic.
     * 
     * @param event The {@link PostEvent} received from the queue.
     */
    @RabbitListener(queues = RabbitConfig.POST_PUBLISHED_QUEUE)
    public void consumePostPublishedEvent(PostEvent event) {
        log.info("Received RabbitMQ event for new post: '{}' by {}", event.getTitle(), event.getAuthorName());
        
        // Construct the post link using the standard frontend routing with slug
        String postLink = "http://localhost:4200/blog/" + (event.getSlug() != null ? event.getSlug() : event.getPostId());
        
        try {
            newsletterService.notifyNewPost(event.getTitle(), postLink);
            log.info("Successfully processed newsletter campaign for post ID: {}", event.getPostId());
        } catch (Exception e) {
            log.error("Failed to process newsletter event for post ID {}: {}", event.getPostId(), e.getMessage());
        }
    }
}
