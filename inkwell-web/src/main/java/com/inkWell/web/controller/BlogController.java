package com.inkWell.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BlogController {

    private final RestTemplate restTemplate;
    private static final String POST_SERVICE_URL = "http://POST-SERVICE/posts";

    @GetMapping("/")
    public String index(Model model) {
        try {
            List<?> posts = restTemplate.getForObject(POST_SERVICE_URL, List.class);
            model.addAttribute("posts", posts);
        } catch (Exception e) {
            model.addAttribute("posts", List.of());
        }
        return "index";
    }
}
