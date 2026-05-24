package com.example.ledgerview.config;

import com.example.ledgerview.config.security.MvcSecurityConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(MvcSecurityConfiguration.class)
public class LedgerViewConfig {
}
