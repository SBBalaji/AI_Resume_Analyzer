package com.balaji.resumeanalyzer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // ❌ Disable CSRF (important for APIs like yours)
            .csrf(csrf -> csrf.disable())

            // ❌ Disable default login page (Spring Security default)
            .formLogin(form -> form.disable())

            // ❌ Disable HTTP Basic auth popup
            .httpBasic(basic -> basic.disable())

            // ✅ Allow all requests (for now)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/",
                        "/index.html",
                        "/signup.html",
                        "/login.html",
                        "/admin.html",
                        "/css/**",
                        "/js/**",
                        "/api/auth/**",
                        "/api/resume/**"
                ).permitAll()

                // Any other request also allowed (you can restrict later)
                .anyRequest().permitAll()
            );

        return http.build();
    }
}