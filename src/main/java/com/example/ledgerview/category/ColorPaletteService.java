package com.example.ledgerview.category;

import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
@Getter
public class ColorPaletteService {

    private final ColorPaletteProperties properties;

    public ColorPaletteService(ColorPaletteProperties properties) {
        this.properties = properties;
    }

    public String normalize(String color) {
        if (color != null && properties.colors().contains(color)) {
            return color;
        }
        return properties.defaultColor();
    }
}
