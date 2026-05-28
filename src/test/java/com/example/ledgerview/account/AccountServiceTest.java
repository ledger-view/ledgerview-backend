package com.example.ledgerview.account;

import com.example.ledgerview.currency.CurrencyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    AccountRepository repository;

    @Mock
    CurrencyService currencyService;

    @InjectMocks
    AccountService service;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID ACCOUNT_ID = UUID.randomUUID();

    @Test
    void create_setsOpeningBalanceAndCurrency() {
        var req = new AccountController.AccountCreateRequest(
                "Main Checking", "Chase", AccountType.CHECKING, "USD", new BigDecimal("1500.00"), null);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.create(USER_ID, req);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(repository).save(captor.capture());
        Account saved = captor.getValue();
        assertThat(saved.getCurrency()).isEqualTo("USD");
        assertThat(saved.getBalance()).isEqualByComparingTo("1500.00");
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
    }

    @Test
    void update_doesNotOverrideCurrencyOrBalance() {
        Account existing = new Account();
        existing.setId(ACCOUNT_ID);
        existing.setCurrency("EUR");
        existing.setBalance(new BigDecimal("4235.40"));
        existing.setName("Old Name");
        when(repository.findByIdAndUserId(ACCOUNT_ID, USER_ID)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var req = new AccountController.AccountUpdateRequest("New Name", "Chase", AccountType.CHECKING, null);
        Account result = service.update(USER_ID, ACCOUNT_ID, req);

        assertThat(result.getCurrency()).isEqualTo("EUR");
        assertThat(result.getBalance()).isEqualByComparingTo("4235.40");
        assertThat(result.getName()).isEqualTo("New Name");
    }

    @Test
    void update_throwsNotFound_whenAccountMissingOrBelongsToDifferentUser() {
        when(repository.findByIdAndUserId(any(), any())).thenReturn(Optional.empty());

        var req = new AccountController.AccountUpdateRequest("Name", "Bank", AccountType.CHECKING, null);
        assertThatThrownBy(() -> service.update(USER_ID, ACCOUNT_ID, req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void delete_throwsNotFound_whenAccountMissingOrBelongsToDifferentUser() {
        when(repository.findByIdAndUserId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(USER_ID, ACCOUNT_ID))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
