package com.demo.usuario.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Value("${app.security.internal-api-key}")
    private String internalApiKey;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .addFilterBefore(new ApiKeyFilter(internalApiKey), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    static class ApiKeyFilter extends OncePerRequestFilter {
        private final String expectedApiKey;
        ApiKeyFilter(String expectedApiKey) { this.expectedApiKey = expectedApiKey; }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {
            String path = request.getRequestURI();
            boolean protectedApi = path.startsWith("/api/");
            if (protectedApi) {
                String apiKey = request.getHeader("X-Internal-Api-Key");
                if (!expectedApiKey.equals(apiKey)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Missing or invalid X-Internal-Api-Key");
                    return;
                }
            }
            chain.doFilter(request, response);
        }
    }
}
