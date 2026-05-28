package com.example.ledgerview.config;

import com.example.ledgerview.category.ColorPaletteProperties;
import com.example.ledgerview.config.security.MvcSecurityConfiguration;
import com.example.ledgerview.currency.CurrencyProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(MvcSecurityConfiguration.class)
@EnableConfigurationProperties({ColorPaletteProperties.class, CurrencyProperties.class})
public class LedgerViewConfig {
}
