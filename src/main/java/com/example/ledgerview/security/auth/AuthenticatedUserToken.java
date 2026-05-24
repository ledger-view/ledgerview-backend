package com.example.ledgerview.security.auth;

import com.example.ledgerview.security.user.AuthenticatedUser;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;

public final class AuthenticatedUserToken extends AbstractAuthenticationToken {

    private final AuthenticatedUser principal;
    private final Jwt jwt;

    public AuthenticatedUserToken(AuthenticatedUser principal, Jwt jwt, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.jwt = jwt;
        super.setAuthenticated(true);
    }

    @Override
    public AuthenticatedUser getPrincipal() {
        return principal;
    }

    @Override
    public Jwt getCredentials() {
        return jwt;
    }
}
