package com.example.ledgerview.transaction;

import com.example.ledgerview.account.Account;
import com.example.ledgerview.account.AccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository repository;
    private final AccountRepository accountRepository;

    public TransactionService(TransactionRepository repository, AccountRepository accountRepository) {
        this.repository = repository;
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public Page<Transaction> list(UUID userId, UUID accountId, UUID categoryId, TransactionType type,
                                  Instant dateFrom, Instant dateTo,
                                  int page, int size, String sortField, String sortDir) {
        Specification<Transaction> spec = TransactionSpec.hasUserId(userId);
        if (accountId != null) spec = spec.and(TransactionSpec.hasAccountId(accountId));
        if (categoryId != null) spec = spec.and(TransactionSpec.hasCategoryId(categoryId));
        if (type != null) spec = spec.and(TransactionSpec.hasType(type));
        if (dateFrom != null) spec = spec.and(TransactionSpec.dateFrom(dateFrom));
        if (dateTo != null) spec = spec.and(TransactionSpec.dateTo(dateTo));

        Pageable pageable = PageRequest.of(page, Math.min(size, 100), buildSort(sortField, sortDir));
        return repository.findAll(spec, pageable);
    }

    @Transactional
    public Transaction create(UUID userId, TransactionController.TransactionRequest req) {
        Account account = resolveAccount(userId, req.accountId());
        Transaction tx = new Transaction();
        tx.setUserId(userId);
        apply(tx, req, account.getCurrency());
        account.setBalance(account.getBalance().add(req.amount()));
        accountRepository.save(account);
        return repository.save(tx);
    }

    @Transactional
    public Transaction update(UUID userId, UUID id, TransactionController.TransactionRequest req) {
        Transaction tx = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UUID oldAccountId = tx.getAccountId();
        BigDecimal oldAmount = tx.getAmount();
        Account account = resolveAccount(userId, req.accountId());
        if (oldAccountId.equals(req.accountId())) {
            account.setBalance(account.getBalance().subtract(oldAmount).add(req.amount()));
            accountRepository.save(account);
        } else {
            Account oldAccount = resolveAccount(userId, oldAccountId);
            oldAccount.setBalance(oldAccount.getBalance().subtract(oldAmount));
            accountRepository.save(oldAccount);
            account.setBalance(account.getBalance().add(req.amount()));
            accountRepository.save(account);
        }
        apply(tx, req, account.getCurrency());
        return repository.save(tx);
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        Transaction tx = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Account account = resolveAccount(userId, tx.getAccountId());
        account.setBalance(account.getBalance().subtract(tx.getAmount()));
        accountRepository.save(account);
        repository.delete(tx);
    }

    private Account resolveAccount(UUID userId, UUID accountId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
    }

    private void apply(Transaction tx, TransactionController.TransactionRequest req, String currency) {
        tx.setTitle(req.title());
        tx.setAmount(req.amount());
        tx.setType(req.type());
        tx.setCurrency(currency);
        tx.setDate(req.date());
        tx.setCategoryId(req.categoryId());
        tx.setAccountId(req.accountId());
        tx.setNote(req.note());
    }

    private Sort buildSort(String field, String dir) {
        Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return switch (field) {
            case "amount" -> Sort.by(direction, "amount");
            case "title" -> Sort.by(direction, "title");
            default -> Sort.by(direction, "date");
        };
    }
}
