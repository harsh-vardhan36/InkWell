package com.inkWell.newsletter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class NewsletterApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewsletterApplication.class, args);
    }
}
