package com.inkWell.newsletter.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for RabbitMQ messaging.
 * Defines queues, exchanges, and bindings for post-published events.
 */
@Configuration
public class RabbitConfig {

    public static final String POST_PUBLISHED_QUEUE = "post.published.queue";
    public static final String POST_EXCHANGE = "post.exchange";
    public static final String POST_ROUTING_KEY = "post.published.routingKey";

    /**
     * Defines the queue for post-published events.
     * 
     * @return A {@link Queue} object.
     */
    @Bean
    public Queue postQueue() {
        return new Queue(POST_PUBLISHED_QUEUE);
    }

    /**
     * Defines the topic exchange for post-related events.
     * 
     * @return A {@link TopicExchange} object.
     */
    @Bean
    public TopicExchange postExchange() {
        return new TopicExchange(POST_EXCHANGE);
    }

    /**
     * Binds the post queue to the post exchange with a specific routing key.
     * 
     * @param postQueue The queue to bind.
     * @param postExchange The exchange to bind to.
     * @return A {@link Binding} object.
     */
    @Bean
    public Binding postBinding(Queue postQueue, TopicExchange postExchange) {
        return BindingBuilder.bind(postQueue).to(postExchange).with(POST_ROUTING_KEY);
    }

    /**
     * Configures the message converter for JSON serialization/deserialization.
     * 
     * @return A {@link MessageConverter} using Jackson.
     */
    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Configures the RabbitTemplate with the custom message converter.
     * 
     * @param connectionFactory The connection factory to use.
     * @return An {@link AmqpTemplate} for sending/receiving messages.
     */
    @Bean
    public AmqpTemplate template(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }
}
