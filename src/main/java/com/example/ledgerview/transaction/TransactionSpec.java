package com.example.ledgerview.transaction;

import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.UUID;

class TransactionSpec {

    static Specification<Transaction> hasUserId(UUID userId) {
        return (root, query, cb) -> cb.equal(root.get("userId"), userId);
    }

    static Specification<Transaction> hasAccountId(UUID accountId) {
        return (root, query, cb) -> cb.equal(root.get("accountId"), accountId);
    }

    static Specification<Transaction> hasCategoryId(UUID categoryId) {
        return (root, query, cb) -> cb.equal(root.get("categoryId"), categoryId);
    }

    static Specification<Transaction> hasType(TransactionType type) {
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    static Specification<Transaction> dateFrom(Instant from) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("date"), from);
    }

    static Specification<Transaction> dateTo(Instant to) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("date"), to);
    }
}
