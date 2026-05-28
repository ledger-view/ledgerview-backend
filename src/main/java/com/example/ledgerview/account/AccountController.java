package com.example.ledgerview.account;

import com.example.ledgerview.security.user.AuthenticatedUser;
import com.example.ledgerview.util.ApiPaths;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiPaths.ACCOUNTS)
public class AccountController {

    record AccountCreateRequest(
            @NotBlank String name,
            @NotBlank String institution,
            @NotNull AccountType type,
            @NotBlank String currency,
            @NotNull BigDecimal balance,
            String number
    ) {
    }

    record AccountUpdateRequest(
            @NotBlank String name,
            @NotBlank String institution,
            @NotNull AccountType type,
            String number
    ) {
    }

    record AccountResponse(
            UUID id,
            String name,
            String institution,
            AccountType type,
            String currency,
            BigDecimal balance,
            String number
    ) {
    }

    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @GetMapping
    public List<AccountResponse> list(@AuthenticationPrincipal AuthenticatedUser user) {
        return service.list(user.id()).stream().map(this::toResponse).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@RequestBody @Valid AccountCreateRequest req,
                                  @AuthenticationPrincipal AuthenticatedUser user) {
        return toResponse(service.create(user.id(), req));
    }

    @PutMapping("/{id}")
    public AccountResponse update(@PathVariable UUID id,
                                  @RequestBody @Valid AccountUpdateRequest req,
                                  @AuthenticationPrincipal AuthenticatedUser user) {
        return toResponse(service.update(user.id(), id, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser user) {
        service.delete(user.id(), id);
    }

    private AccountResponse toResponse(Account a) {
        return new AccountResponse(a.getId(), a.getName(), a.getInstitution(),
                a.getType(), a.getCurrency(), a.getBalance(), a.getNumber());
    }
}
