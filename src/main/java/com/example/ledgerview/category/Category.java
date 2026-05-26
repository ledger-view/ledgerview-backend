package com.example.ledgerview.category;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "categories", schema = "ledgerview")
public class Category {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(nullable = false, length = 16)
    private String color;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 16)
    private CategoryType type;
}
