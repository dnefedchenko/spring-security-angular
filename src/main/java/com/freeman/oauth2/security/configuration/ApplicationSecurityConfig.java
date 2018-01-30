package com.freeman.oauth2.security.configuration;

import com.freeman.oauth2.security.OAuth2AuthenticationSuccessHandler;
import com.freeman.oauth2.security.service.ApplicationUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class ApplicationSecurityConfig extends WebSecurityConfigurerAdapter {
    private Logger logger = LoggerFactory.getLogger(ApplicationSecurityConfig.class);

    private static String GOOGLE_SIGN_IN_URL = "/authenticate/google";

    private RestTemplate restTemplate;

    @Autowired private Environment environment;

    @Autowired private AuthenticationSuccessHandler authenticationSuccessHandler;
    @Autowired private AuthenticationFailureHandler authenticationFailureHandler;
    @Autowired private LogoutSuccessHandler logoutSuccessHandler;

    @Autowired private RestfulAuthenticationEntryPoint restfulAuthenticationEntryPoint;

    public ApplicationSecurityConfig(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    @Autowired
    public void initializeParentAuthenticationManager(AuthenticationManagerBuilder builder, DaoAuthenticationProvider daoAuthenticationProvider) {
        builder.authenticationProvider(daoAuthenticationProvider);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/**.html", "/**.bundle.js", "/**.js.map", "/**.jpg", "/**.ttf");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .exceptionHandling()
                .authenticationEntryPoint(restfulAuthenticationEntryPoint)
            .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers("/api/ping").permitAll()
                .anyRequest().authenticated()
            .and()
                .formLogin()
                    .loginProcessingUrl("/authenticate/login")
                    .successHandler(authenticationSuccessHandler)
                    .failureHandler(authenticationFailureHandler)
                .permitAll()
            .and()
                .logout()
                    .logoutUrl("/authenticate/logout")
                    .logoutSuccessHandler(logoutSuccessHandler)
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .deleteCookies("JSESSIONID")
                .permitAll()
            .and()
                .oauth2Login()
                .failureHandler(authenticationFailureHandler)
                .permitAll()
            .and()
                .addFilterAfter(buildGoogleImplicitGrantFilter(), OAuth2LoginAuthenticationFilter.class)
                .cors()
            .and()
                .csrf().disable();
    }

    private AbstractAuthenticationProcessingFilter buildGoogleImplicitGrantFilter() {
        OAuth2ImplicitGrantAuthenticationFilter filter =
                new OAuth2ImplicitGrantAuthenticationFilter(GOOGLE_SIGN_IN_URL, new OAuth2AuthenticationSuccessHandler());
        filter.setRestTemplate(this.restTemplate);
        return filter;
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new HttpStatusReturningLoginSuccessHandler();
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return new SimpleUrlAuthenticationFailureHandler();
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return new HttpStatusReturningLogoutSuccessHandler();
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(PasswordEncoder passwordEncoder, UserDetailsService userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(11);
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new ApplicationUserDetailsService();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        String[] allowedOrigins = environment.getProperty("cors.allowed.origins").split(",");
        String[] allowedHeaders = environment.getProperty("cors.allowed.headers").split(",");
        String[] allowedMethods = environment.getProperty("cors.allowed.methods").split(",");
        Long maxAge = Long.parseLong(environment.getProperty("cors.max.age"));
        Boolean allowCredentials = Boolean.parseBoolean(environment.getProperty("cors.allow.credentials"));

        logger.info("******************* CORS Configuration *******************");
        logger.info("* Allowed origins: " + Arrays.asList(allowedOrigins).toString());
        logger.info("* Allowed headers: " + Arrays.asList(allowedHeaders).toString());
        logger.info("* Allowed methods: " + Arrays.asList(allowedMethods).toString());
        logger.info("* Allow credentials: " + allowCredentials);
        logger.info("* Max age: " + maxAge);
        logger.info("**********************************************************");

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        configuration.setAllowedMethods(Arrays.asList(allowedMethods));
        configuration.setAllowedHeaders(Arrays.asList(allowedHeaders));
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
