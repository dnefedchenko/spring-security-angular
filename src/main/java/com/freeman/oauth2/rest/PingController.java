package com.freeman.oauth2.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/ping")
public class PingController {
    @GetMapping
    public Map<String, String> greet(@RequestParam String pong) {
        Map<String, String> response = new HashMap<>();
        response.put("ping", String.format("Hi, %s", pong));
        return response;
    }
}
