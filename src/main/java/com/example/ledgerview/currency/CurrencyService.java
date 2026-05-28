package com.example.ledgerview.currency;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Stream;

@Component
public class CurrencyService {

    private final CurrencyProperties properties;
    private final List<String> all;

    public CurrencyService(CurrencyProperties properties) {
        this.properties = properties;
        this.all = Stream.concat(properties.fiat().stream(), properties.crypto().stream()).toList();
    }

    public CurrencyProperties grouped() {
        return properties;
    }

    public void validate(String currency) {
        if (currency == null || !all.contains(currency)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported currency: " + currency);
        }
    }
}
