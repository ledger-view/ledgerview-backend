package com.example.ledgerview.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    List<Account> findAllByUserIdOrderByName(UUID userId);

    Optional<Account> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a WHERE a.userId = :userId")
    BigDecimal sumBalanceByUserId(@Param("userId") UUID userId);
}
