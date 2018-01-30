package com.freeman.oauth2.rest;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping(value = "/api/users")
public class UsersController {
    @GetMapping(value = "/user")
    public UserDetails currentUser(@AuthenticationPrincipal User user) {
        return Objects.nonNull(user) ? user : checkOAuth2Principal();
    }

    private UserDetails checkOAuth2Principal() {
        OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        OAuth2User user = oauth2Token.getPrincipal();
        return User
                .withUsername(user.getAttributes().get("email").toString())
                .password(oauth2Token.getCredentials().toString())
                .authorities(oauth2Token.getAuthorities())
                .build();
    }
}
