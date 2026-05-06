package com.inkWell.api_gateway.resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class FallbackController {

    @GetMapping("/fallback")
    public Mono<Map<String, String>> fallback() {
        return Mono.just(Map.of(
            "error", "Service Unavailable",
            "message", "The requested service is currently down or taking too long to respond. Please try again later."
        ));
    }
}
