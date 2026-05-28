package com.example.ledgerview.transaction;

import com.example.ledgerview.account.Account;
import com.example.ledgerview.account.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    TransactionRepository repository;
    @Mock
    AccountRepository accountRepository;
    @InjectMocks
    TransactionService service;

    static final UUID USER_ID = UUID.randomUUID();
    static final UUID ACCOUNT_A = UUID.randomUUID();
    static final UUID ACCOUNT_B = UUID.randomUUID();
    static final UUID CATEGORY = UUID.randomUUID();
    static final UUID TX_ID = UUID.randomUUID();

    Account account(UUID id, String balance) {
        Account a = new Account();
        a.setId(id);
        a.setUserId(USER_ID);
        a.setCurrency("USD");
        a.setBalance(new BigDecimal(balance));
        return a;
    }

    Transaction transaction(UUID accountId, String amount) {
        Transaction tx = new Transaction();
        tx.setId(TX_ID);
        tx.setUserId(USER_ID);
        tx.setAccountId(accountId);
        tx.setCategoryId(CATEGORY);
        tx.setAmount(new BigDecimal(amount));
        tx.setType(TransactionType.EXPENSE);
        tx.setDate(Instant.now());
        tx.setCurrency("USD");
        tx.setTitle("Test");
        return tx;
    }

    TransactionController.TransactionRequest request(UUID accountId, String amount) {
        return new TransactionController.TransactionRequest(
                "Test", new BigDecimal(amount), TransactionType.EXPENSE,
                Instant.now(), CATEGORY, accountId, null);
    }

    @Test
    void create_addsAmountToAccountBalance() {
        when(accountRepository.findByIdAndUserId(ACCOUNT_A, USER_ID)).thenReturn(Optional.of(account(ACCOUNT_A, "1000.00")));
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.create(USER_ID, request(ACCOUNT_A, "-50.00"));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertThat(captor.getValue().getBalance()).isEqualByComparingTo("950.00");
    }

    @Test
    void update_sameAccount_appliesNetDelta() {
        when(repository.findByIdAndUserId(TX_ID, USER_ID)).thenReturn(Optional.of(transaction(ACCOUNT_A, "-50.00")));
        when(accountRepository.findByIdAndUserId(ACCOUNT_A, USER_ID)).thenReturn(Optional.of(account(ACCOUNT_A, "950.00")));
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.update(USER_ID, TX_ID, request(ACCOUNT_A, "-100.00"));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        // 950 + 50 (revert) - 100 (new) = 900
        assertThat(captor.getValue().getBalance()).isEqualByComparingTo("900.00");
    }

    @Test
    void update_differentAccount_revertsOldAndAppliesNew() {
        when(repository.findByIdAndUserId(TX_ID, USER_ID)).thenReturn(Optional.of(transaction(ACCOUNT_A, "-50.00")));
        when(accountRepository.findByIdAndUserId(ACCOUNT_A, USER_ID)).thenReturn(Optional.of(account(ACCOUNT_A, "950.00")));
        when(accountRepository.findByIdAndUserId(ACCOUNT_B, USER_ID)).thenReturn(Optional.of(account(ACCOUNT_B, "1000.00")));
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.update(USER_ID, TX_ID, request(ACCOUNT_B, "-80.00"));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository, times(2)).save(captor.capture());
        List<Account> saved = captor.getAllValues();

        assertThat(saved).anySatisfy(a -> {
            assertThat(a.getId()).isEqualTo(ACCOUNT_A);
            assertThat(a.getBalance()).isEqualByComparingTo("1000.00"); // reverted
        });
        assertThat(saved).anySatisfy(a -> {
            assertThat(a.getId()).isEqualTo(ACCOUNT_B);
            assertThat(a.getBalance()).isEqualByComparingTo("920.00"); // 1000 - 80
        });
    }

    @Test
    void delete_subtractsAmountFromAccountBalance() {
        when(repository.findByIdAndUserId(TX_ID, USER_ID)).thenReturn(Optional.of(transaction(ACCOUNT_A, "-50.00")));
        when(accountRepository.findByIdAndUserId(ACCOUNT_A, USER_ID)).thenReturn(Optional.of(account(ACCOUNT_A, "950.00")));

        service.delete(USER_ID, TX_ID);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertThat(captor.getValue().getBalance()).isEqualByComparingTo("1000.00"); // restored
    }
}
