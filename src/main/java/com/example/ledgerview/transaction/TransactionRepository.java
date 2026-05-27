package com.example.ledgerview.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);

    List<Transaction> findByUserIdAndTypeAndDateBetween(UUID userId, TransactionType type, Instant from, Instant to);

    @Query("SELECT t.categoryId, COUNT(t) FROM Transaction t WHERE t.userId = :userId GROUP BY t.categoryId")
    List<Object[]> countGroupByCategoryId(@Param("userId") UUID userId);

}
