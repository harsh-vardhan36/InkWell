package com.inkWell.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final CustomOAuth2UserService customOAuth2UserService;
        private final OAuth2SuccessHandler oauth2SuccessHandler;
        private final ClientRegistrationRepository clientRegistrationRepository;

        @Value("${internal.secret:change-me-in-env}")
        private String internalSecret;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .formLogin(AbstractHttpConfigurer::disable)
                                .httpBasic(AbstractHttpConfigurer::disable)
                                .authorizeHttpRequests(auth -> auth
                                                // Only allow requests that came through the gateway
                                                // (gateway adds X-Internal-Secret header)
                                                .requestMatchers("/auth/**", "/login/**", "/oauth2/**",
                                                                 "/login/oauth2/**", "/error",
                                                                 "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                // ✅ FIX: Use IF_REQUIRED, NOT STATELESS
                                // OAuth2 needs a temporary session to store state between
                                // the initial redirect and the callback. JWT makes the rest stateless.
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                                .oauth2Login(oauth2 -> oauth2
                                                .authorizationEndpoint(authorization -> authorization
                                                                .authorizationRequestResolver(new CustomAuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization")))
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(customOAuth2UserService))
                                                .successHandler(oauth2SuccessHandler))
                                // ✅ ADD: Reject requests missing the gateway's internal secret header
                                .addFilterBefore(new GatewayOnlyFilter(internalSecret),
                                              UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public org.springframework.web.client.RestTemplate restTemplate() {
                return new org.springframework.web.client.RestTemplate();
        }
}