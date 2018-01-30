package com.freeman.oauth2.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/greet")
public class GreetingController {
    @GetMapping
    public Map<String, String> greet(@RequestParam String name) {
        Map<String, String> response = new HashMap<>();
        response.put("message", String.format("Hi, %s", name));
        return response;
    }
}
