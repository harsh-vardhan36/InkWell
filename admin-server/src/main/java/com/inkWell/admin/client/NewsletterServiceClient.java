package com.inkWell.admin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "newsletter-service", path = "/newsletter/admin")
public interface NewsletterServiceClient {

    @GetMapping("/subscribers")
    List<Object> getAllSubscribers();

    @PostMapping("/send")
    Map<String, String> sendNewsletter(@RequestBody Object newsletterRequest);
}
