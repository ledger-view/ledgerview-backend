package com.example.ledgerview.security;

import com.example.ledgerview.security.user.AuthenticatedUser;
import com.example.ledgerview.security.user.UserResource;
import com.example.ledgerview.util.ApiPaths;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.SECURITY_ME)
public class SecurityController {

    @GetMapping
    public UserResource user(@AuthenticationPrincipal AuthenticatedUser user) {
        return new UserResource(user.id(), user.email(), user.fullName());
    }
}
