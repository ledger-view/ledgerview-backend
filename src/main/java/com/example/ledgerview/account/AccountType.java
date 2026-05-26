package com.example.ledgerview.account;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AccountType {
    CHECKING, SAVINGS, CASH, CRYPTO;

    @JsonValue
    public String jsonValue() {
        String name = name();
        return name.charAt(0) + name.substring(1).toLowerCase();
    }

    @JsonCreator
    public static AccountType from(String value) {
        return valueOf(value.toUpperCase());
    }
}
