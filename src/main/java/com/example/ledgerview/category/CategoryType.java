package com.example.ledgerview.category;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum CategoryType {
    INCOME, EXPENSE, TRANSFER;

    @JsonCreator
    public static CategoryType from(String value) {
        return valueOf(value.toUpperCase());
    }
}
