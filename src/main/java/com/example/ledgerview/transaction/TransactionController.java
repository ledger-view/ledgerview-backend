package com.example.ledgerview.transaction;

import com.example.ledgerview.security.user.AuthenticatedUser;
import com.example.ledgerview.util.ApiPaths;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping(ApiPaths.TRANSACTIONS)
public class TransactionController {

    record TransactionRequest(
            @NotBlank String title,
            @NotNull BigDecimal amount,
            @NotNull TransactionType type,
            @NotNull Instant date,
            @NotNull UUID categoryId,
            @NotNull UUID accountId,
            String note
    ) {}

    record TransactionResponse(
            UUID id,
            String title,
            BigDecimal amount,
            TransactionType type,
            Instant date,
            UUID categoryId,
            UUID accountId,
            String note
    ) {}

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @GetMapping
    public Page<TransactionResponse> list(
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) Instant dateFrom,
            @RequestParam(required = false) Instant dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "date") String sort,
            @RequestParam(defaultValue = "desc") String dir,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return service.list(user.id(), accountId, categoryId, type, dateFrom, dateTo, page, size, sort, dir)
                .map(this::toResponse);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(@RequestBody @Valid TransactionRequest req,
                                      @AuthenticationPrincipal AuthenticatedUser user) {
        return toResponse(service.create(user.id(), req));
    }

    @PutMapping("/{id}")
    public TransactionResponse update(@PathVariable UUID id,
                                      @RequestBody @Valid TransactionRequest req,
                                      @AuthenticationPrincipal AuthenticatedUser user) {
        return toResponse(service.update(user.id(), id, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser user) {
        service.delete(user.id(), id);
    }

    private TransactionResponse toResponse(Transaction t) {
        return new TransactionResponse(t.getId(), t.getTitle(), t.getAmount(), t.getType(),
                t.getDate(), t.getCategoryId(), t.getAccountId(), t.getNote());
    }
}
