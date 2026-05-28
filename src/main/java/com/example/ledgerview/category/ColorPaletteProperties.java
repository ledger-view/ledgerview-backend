package com.example.ledgerview.category;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "application.palette")
public record ColorPaletteProperties(List<String> colors, String defaultColor) {}
