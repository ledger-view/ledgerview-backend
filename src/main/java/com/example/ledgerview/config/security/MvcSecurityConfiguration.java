package com.example.ledgerview.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
@Import(SecurityConfiguration.class)
public class MvcSecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, SecurityConfigurer securityConfigurer) throws Exception {
        securityConfigurer.configure(httpSecurity);
        return httpSecurity.build();
    }
}
