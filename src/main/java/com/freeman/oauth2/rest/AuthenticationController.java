package com.freeman.oauth2.rest;

import com.freeman.oauth2.security.model.GoogleUserInfo;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping(value = "/authenticate")
public class AuthenticationController {
    private final RestTemplate restTemplate;

    public AuthenticationController(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    /*@PostMapping(value = "/google")
    public @ResponseBody UserDetails signInWithGoogle(@RequestParam ("accessToken") String accessToken) {
        String tokenValidationUrl = "https://www.googleapis.com/oauth2/v3/tokeninfo?access_token=%s";
        String validationResponse = restTemplate.getForObject(String.format(tokenValidationUrl, accessToken), String.class);
        if (validationResponse.contains("invalid_token")) {
            throw new RuntimeException("Invalid access token");
        }

        String userInfoEndpoint = "https://www.googleapis.com/oauth2/v3/userinfo?access_token=%s";
        GoogleUserInfo userInfoResponse = restTemplate.getForObject(String.format(userInfoEndpoint, accessToken), GoogleUserInfo.class);

        UserDetails principal = User.withUsername(userInfoResponse.getEmail()).password("*****").authorities("USER").build();
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(principal, "secret");

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(token);
        return principal;
    }*/
}
