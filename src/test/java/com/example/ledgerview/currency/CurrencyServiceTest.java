package com.example.ledgerview.currency;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrencyServiceTest {

    private final CurrencyService service = new CurrencyService(
            new CurrencyProperties(List.of("USD", "EUR"), List.of("BTC", "ETH"))
    );

    @Test
    void validate_acceptsKnownFiat() {
        assertThatNoException().isThrownBy(() -> service.validate("USD"));
    }

    @Test
    void validate_acceptsKnownCrypto() {
        assertThatNoException().isThrownBy(() -> service.validate("BTC"));
    }

    @Test
    void validate_rejectsUnknownCurrency() {
        assertThatThrownBy(() -> service.validate("XYZ"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Unsupported currency");
    }

    @Test
    void validate_rejectsNull() {
        assertThatThrownBy(() -> service.validate(null))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
