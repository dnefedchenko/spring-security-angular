package com.freeman.oauth2.security.configuration;

import com.freeman.oauth2.security.model.GoogleUserInfo;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class OAuth2ImplicitGrantAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private RestTemplate restTemplate;

    public OAuth2ImplicitGrantAuthenticationFilter(String defaultFilterProcessesUrl, AuthenticationSuccessHandler successHandler) {
        super(defaultFilterProcessesUrl);
        setAuthenticationSuccessHandler(successHandler);
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        String accessToken = request.getParameter("accessToken");
        String tokenValidationUrl = "https://www.googleapis.com/oauth2/v3/tokeninfo?access_token=%s";
        String validationResponse = restTemplate.getForObject(String.format(tokenValidationUrl, accessToken), String.class);
        if (validationResponse.contains("invalid_token")) {
            throw new RuntimeException("Invalid access token");
        }

        String userInfoEndpoint = "https://www.googleapis.com/oauth2/v3/userinfo?access_token=%s";
        GoogleUserInfo userInfoResponse = restTemplate.getForObject(String.format(userInfoEndpoint, accessToken), GoogleUserInfo.class);
        UserDetails principal = User.withUsername(userInfoResponse.getEmail()).password("*****").authorities("USER").build();
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(principal, "secret");
        return token;
    }
}
