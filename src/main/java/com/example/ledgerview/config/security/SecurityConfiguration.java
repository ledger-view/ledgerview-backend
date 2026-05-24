package com.example.ledgerview.config.security;

import com.example.ledgerview.security.SecurityController;
import com.example.ledgerview.security.auth.KeycloakJwtAuthenticationConverter;
import com.example.ledgerview.security.user.UserRepository;
import com.example.ledgerview.security.user.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class SecurityConfiguration {

    @Bean
    public SecurityController securityController() {
        return new SecurityController();
    }

    @Bean
    public UserService userService(UserRepository userRepository) {
        return new UserService(userRepository);
    }

    @Configuration
    @Profile("!unsecured")
    public static class SecuredConfiguration {

        @Bean
        public KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter(UserService userService) {
            return new KeycloakJwtAuthenticationConverter(userService);
        }

        @Bean
        public SecuredSecurityConfigurer securedSecurityConfigurer(
                KeycloakJwtAuthenticationConverter jwtAuthenticationConverter
        ) {
            return new SecuredSecurityConfigurer(jwtAuthenticationConverter);
        }
    }

    @Configuration
    @Profile("unsecured")
    public static class UnsecuredConfiguration {

        @Bean
        public UnsecuredSecurityConfigurer unsecuredSecurityConfigurer() {
            return new UnsecuredSecurityConfigurer();
        }
    }
}
