package com.example.ledgerview.account;

import com.example.ledgerview.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("unsecured")
class AccountCurrencyTriggerTest extends PostgresIntegrationTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void directSqlUpdate_ofCurrency_isRejectedByTrigger() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        jdbcTemplate.update(
                "INSERT INTO ledgerview.users (id, email, external_id) VALUES (?, ?, ?)",
                userId, "trigger-test@example.com", "trigger-test-ext-" + userId);
        jdbcTemplate.update(
                "INSERT INTO ledgerview.accounts (id, user_id, name, institution, type, currency, balance) VALUES (?, ?, ?, ?, ?, ?, ?)",
                accountId, userId, "Test Account", "Test Bank", "CHECKING", "USD", BigDecimal.ZERO);

        assertThatThrownBy(() ->
                jdbcTemplate.update(
                        "UPDATE ledgerview.accounts SET currency = 'EUR' WHERE id = ?", accountId))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("account currency cannot be changed after creation");
    }
}
