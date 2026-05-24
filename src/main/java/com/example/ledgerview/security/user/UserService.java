package com.example.ledgerview.security.user;

import org.springframework.transaction.annotation.Transactional;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User createOrUpdate(String externalId, String email) {
        return userRepository.findByExternalId(externalId)
                .map(existingUser -> {
                    existingUser.setEmail(email);
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    User user = new User();
                    user.setEmail(email);
                    user.setExternalId(externalId);
                    return userRepository.save(user);
                });
    }
}
