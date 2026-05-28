package com.example.ledgerview.category;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ColorPaletteServiceTest {

    private final ColorPaletteService service = new ColorPaletteService(
            new ColorPaletteProperties(List.of("#6366F1", "#3B82F6", "#EF4444"), "#6366F1")
    );

    @Test
    void normalize_returnsColor_whenInPalette() {
        assertThat(service.normalize("#3B82F6")).isEqualTo("#3B82F6");
    }

    @Test
    void normalize_returnsDefault_whenNotInPalette() {
        assertThat(service.normalize("#000000")).isEqualTo("#6366F1");
    }

    @Test
    void normalize_returnsDefault_whenNull() {
        assertThat(service.normalize(null)).isEqualTo("#6366F1");
    }
}
