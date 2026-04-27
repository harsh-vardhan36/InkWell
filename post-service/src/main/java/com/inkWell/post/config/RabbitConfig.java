package com.inkWell.post.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String POST_PUBLISHED_QUEUE = "post.published.queue";
    public static final String POST_EXCHANGE = "post.exchange";
    public static final String POST_ROUTING_KEY = "post.published.routingKey";

    @Bean
    public Queue postQueue() {
        return new Queue(POST_PUBLISHED_QUEUE);
    }

    @Bean
    public TopicExchange postExchange() {
        return new TopicExchange(POST_EXCHANGE);
    }

    @Bean
    public Binding postBinding(Queue postQueue, TopicExchange postExchange) {
        return BindingBuilder.bind(postQueue).to(postExchange).with(POST_ROUTING_KEY);
    }

    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate template(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }
}
