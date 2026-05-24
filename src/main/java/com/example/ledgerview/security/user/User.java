package com.example.ledgerview.security.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(
        name = "users",
        schema = "ledgerview",
        uniqueConstraints = {
                @UniqueConstraint(name = "users_unique_email", columnNames = "email"),
                @UniqueConstraint(name = "users_unique_external_id", columnNames = "external_id")
        }
)
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 128)
    private String email;

    @Column(name = "external_id", nullable = false, length = 256)
    private String externalId;
}