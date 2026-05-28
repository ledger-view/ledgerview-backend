package com.example.ledgerview.account;

import com.example.ledgerview.PostgresIntegrationTest;
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

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("unsecured")
class AccountControllerTest extends PostgresIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private static final String DEV_USER_ID = "00000000-0000-0000-0000-000000000001";

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(
                "INSERT INTO ledgerview.users (id, email, external_id) VALUES (?, ?, ?) ON CONFLICT DO NOTHING",
                UUID.fromString(DEV_USER_ID), "admin@example.com", "keycloak-external-id-admin");
        jdbcTemplate.update(
                "DELETE FROM ledgerview.accounts WHERE user_id = ?",
                UUID.fromString(DEV_USER_ID));
    }

    @Test
    void post_createsAccount_withCurrencyAndBalance() throws Exception {
        var body = Map.of("name", "Main Checking", "institution", "Chase",
                "type", "CHECKING", "currency", "USD", "balance", 1000.00);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.balance").value(1000.00));
    }

    @Test
    void post_missingRequiredField_returns400() throws Exception {
        // currency absent
        var body = Map.of("name", "Checking", "institution", "Chase", "type", "CHECKING", "balance", 0.00);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void put_doesNotChangeCurrencyOrBalance() throws Exception {
        var createBody = Map.of("name", "Savings", "institution", "Marcus",
                "type", "SAVINGS", "currency", "EUR", "balance", 2000.00);

        String createResponse = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBody)))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(createResponse).get("id").asText();

        var updateBody = Map.of("name", "Marcus Savings", "institution", "Marcus", "type", "SAVINGS");

        mockMvc.perform(put("/api/accounts/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Marcus Savings"))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.balance").value(2000.00));
    }

    @Test
    void delete_returns204_andAccountNoLongerInList() throws Exception {
        var body = Map.of("name", "Temp", "institution", "Bank",
                "type", "CHECKING", "currency", "USD", "balance", 0.00);

        String createResponse = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(createResponse).get("id").asText();

        mockMvc.perform(delete("/api/accounts/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + id + "')]").doesNotExist());
    }

    @Test
    void put_unknownId_returns404() throws Exception {
        var body = Map.of("name", "Ghost", "institution", "Nowhere", "type", "CHECKING");

        mockMvc.perform(put("/api/accounts/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }
}
