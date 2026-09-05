package com.example.boxdispatch.common.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public Map<String, String> root() {
        return Map.of(
            "service", "Box Dispatch Service",
            "status", "UP",
            "swagger", "/swagger-ui.html",
            "openapi", "/v3/api-docs"
        );
    }
}