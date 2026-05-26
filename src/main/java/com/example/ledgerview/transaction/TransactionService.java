package com.example.ledgerview.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<Transaction> list(UUID userId, UUID accountId, UUID categoryId, TransactionType type,
                                  LocalDate dateFrom, LocalDate dateTo,
                                  int page, int size, String sortField, String sortDir) {
        Specification<Transaction> spec = TransactionSpec.hasUserId(userId);
        if (accountId != null)  spec = spec.and(TransactionSpec.hasAccountId(accountId));
        if (categoryId != null) spec = spec.and(TransactionSpec.hasCategoryId(categoryId));
        if (type != null)       spec = spec.and(TransactionSpec.hasType(type));
        if (dateFrom != null)   spec = spec.and(TransactionSpec.dateFrom(dateFrom));
        if (dateTo != null)     spec = spec.and(TransactionSpec.dateTo(dateTo));

        Pageable pageable = PageRequest.of(page, Math.min(size, 100), buildSort(sortField, sortDir));
        return repository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public List<Transaction> listByUserAndTypeAndDateBetween(UUID userId, TransactionType type, LocalDate from, LocalDate to) {
        return repository.findByUserIdAndTypeAndDateBetween(userId, type, from, to);
    }

    @Transactional
    public Transaction create(UUID userId, TransactionController.TransactionRequest req) {
        Transaction tx = new Transaction();
        tx.setUserId(userId);
        apply(tx, req);
        return repository.save(tx);
    }

    @Transactional
    public Transaction update(UUID userId, UUID id, TransactionController.TransactionRequest req) {
        Transaction tx = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        apply(tx, req);
        return repository.save(tx);
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        Transaction tx = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        repository.delete(tx);
    }

    private void apply(Transaction tx, TransactionController.TransactionRequest req) {
        tx.setTitle(req.title());
        tx.setAmount(req.amount());
        tx.setType(req.type());
        tx.setDate(req.date());
        tx.setCategoryId(req.categoryId());
        tx.setAccountId(req.accountId());
        tx.setNote(req.note());
    }

    private Sort buildSort(String field, String dir) {
        Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return switch (field) {
            case "amount" -> Sort.by(direction, "amount");
            case "title"  -> Sort.by(direction, "title");
            default       -> Sort.by(direction, "date");
        };
    }
}
