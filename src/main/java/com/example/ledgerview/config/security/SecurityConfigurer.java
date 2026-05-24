package com.example.ledgerview.config.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

public sealed interface SecurityConfigurer permits SecuredSecurityConfigurer, UnsecuredSecurityConfigurer {
    void configure(HttpSecurity httpSecurity) throws Exception;
}