package com.example.ledgerview.security.user;

import java.util.UUID;

public record AuthenticatedUser(UUID id, String externalId, String email, String fullName) {
}
