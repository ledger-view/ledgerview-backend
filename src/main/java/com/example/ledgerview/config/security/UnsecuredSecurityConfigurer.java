package com.example.ledgerview.config.security;

import com.example.ledgerview.security.user.AuthenticatedUser;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.UUID;

public final class UnsecuredSecurityConfigurer implements SecurityConfigurer {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .anonymous(anonymous -> anonymous
                        .principal(devUser())
                        .authorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                )
                .csrf(AbstractHttpConfigurer::disable);
    }

    private AuthenticatedUser devUser() {
        return new AuthenticatedUser(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "keycloak-external-id-admin",
                "admin@example.com",
                "Admin Ledgerview"
        );
    }
}
