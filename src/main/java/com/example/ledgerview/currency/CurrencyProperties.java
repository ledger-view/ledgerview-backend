package com.example.ledgerview.currency;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "application.currencies")
public record CurrencyProperties(List<String> fiat, List<String> crypto) {}
