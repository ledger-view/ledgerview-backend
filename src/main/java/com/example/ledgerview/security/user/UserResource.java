package com.example.ledgerview.security.user;

import java.util.UUID;

public record UserResource(UUID id, String email, String fullName) {
}
