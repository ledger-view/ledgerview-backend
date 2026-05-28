package com.example.ledgerview.transaction;

import com.example.ledgerview.PostgresIntegrationTest;
import com.example.ledgerview.account.AccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("unsecured")
class TransactionControllerTest extends PostgresIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    AccountRepository accountRepository;

    static final UUID DEV_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    UUID accountId;
    UUID categoryId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(
                "INSERT INTO ledgerview.users (id, email, external_id) VALUES (?, ?, ?) ON CONFLICT DO NOTHING",
                DEV_USER_ID, "admin@example.com", "keycloak-external-id-admin");
        jdbcTemplate.update("DELETE FROM ledgerview.transactions WHERE user_id = ?", DEV_USER_ID);
        jdbcTemplate.update("DELETE FROM ledgerview.accounts WHERE user_id = ?", DEV_USER_ID);
        jdbcTemplate.update("DELETE FROM ledgerview.categories WHERE user_id = ?", DEV_USER_ID);

        accountId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO ledgerview.accounts (id, user_id, name, institution, type, currency, balance) VALUES (?, ?, ?, ?, ?, ?, ?)",
                accountId, DEV_USER_ID, "Test", "Bank", "CHECKING", "USD", new BigDecimal("1000.00"));

        categoryId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO ledgerview.categories (id, user_id, name, color, type) VALUES (?, ?, ?, ?, ?)",
                categoryId, DEV_USER_ID, "Food", "#EF4444", "EXPENSE");
    }

    Map<String, Object> txBody(UUID targetAccountId, String amount) {
        Map<String, Object> body = new HashMap<>();
        body.put("title", "Coffee");
        body.put("amount", new BigDecimal(amount));
        body.put("type", "EXPENSE");
        body.put("date", "2026-05-01T10:00:00Z");
        body.put("categoryId", targetAccountId == null ? categoryId.toString() : categoryId.toString());
        body.put("accountId", (targetAccountId != null ? targetAccountId : accountId).toString());
        body.put("note", "");
        return body;
    }

    BigDecimal balanceOf(UUID id) {
        return accountRepository.findByIdAndUserId(id, DEV_USER_ID).orElseThrow().getBalance();
    }

    @Test
    void post_updatesAccountBalance() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(txBody(null, "-50.00"))))
                .andExpect(status().isCreated());

        assertThat(balanceOf(accountId)).isEqualByComparingTo("950.00");
    }

    @Test
    void delete_revertsAccountBalance() throws Exception {
        String response = mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(txBody(null, "-50.00"))))
                .andReturn().getResponse().getContentAsString();

        String txId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(delete("/api/transactions/" + txId))
                .andExpect(status().isNoContent());

        assertThat(balanceOf(accountId)).isEqualByComparingTo("1000.00");
    }

    @Test
    void put_changingAccount_updatesBothBalances() throws Exception {
        UUID accountBId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO ledgerview.accounts (id, user_id, name, institution, type, currency, balance) VALUES (?, ?, ?, ?, ?, ?, ?)",
                accountBId, DEV_USER_ID, "Account B", "Bank", "CHECKING", "USD", new BigDecimal("500.00"));

        String response = mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(txBody(null, "-50.00"))))
                .andReturn().getResponse().getContentAsString();

        String txId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(put("/api/transactions/" + txId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(txBody(accountBId, "-80.00"))))
                .andExpect(status().isOk());

        assertThat(balanceOf(accountId)).isEqualByComparingTo("1000.00");  // reverted
        assertThat(balanceOf(accountBId)).isEqualByComparingTo("420.00");  // 500 - 80
    }

    @Test
    void post_missingRequiredField_returns400() throws Exception {
        Map<String, Object> body = txBody(null, "-50.00");
        body.remove("title");

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }
}
