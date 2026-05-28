package com.example.ledgerview.account;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository repository;

    public AccountService(AccountRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Account> list(UUID userId) {
        return repository.findAllByUserIdOrderByName(userId);
    }

    @Transactional
    public Account create(UUID userId, AccountController.AccountCreateRequest req) {
        Account account = new Account();
        account.setUserId(userId);
        account.setCurrency(req.currency());
        account.setBalance(req.balance());
        apply(account, req.name(), req.institution(), req.type(), req.number());
        return repository.save(account);
    }

    @Transactional
    public Account update(UUID userId, UUID id, AccountController.AccountUpdateRequest req) {
        Account account = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        apply(account, req.name(), req.institution(), req.type(), req.number());
        return repository.save(account);
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        Account account = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        repository.delete(account);
    }

    private void apply(Account account, String name, String institution, AccountType type, String number) {
        account.setName(name);
        account.setInstitution(institution);
        account.setType(type);
        account.setNumber(number);
    }
}
