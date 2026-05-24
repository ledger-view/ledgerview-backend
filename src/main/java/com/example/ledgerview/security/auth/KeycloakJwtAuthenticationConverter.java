package com.example.ledgerview.security.auth;

import com.example.ledgerview.security.user.AuthenticatedUser;
import com.example.ledgerview.security.user.UserService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserService userService;

    public KeycloakJwtAuthenticationConverter(UserService userService) {
        this.userService = userService;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String externalId = jwt.getSubject();
        String email = resolveEmail(jwt);
        String fullName = resolveFullName(jwt);

        var user = userService.createOrUpdate(externalId, email);
        var principal = new AuthenticatedUser(user.getId(), externalId, email, fullName);

        return new AuthenticatedUserToken(principal, jwt, extractRealmRoles(jwt));
    }

    private String resolveEmail(Jwt jwt) {
        String email = jwt.getClaimAsString(StandardClaimNames.EMAIL);
        return email != null ? email : jwt.getClaimAsString(StandardClaimNames.PREFERRED_USERNAME);
    }

    private String resolveFullName(Jwt jwt) {
        String firstName = jwt.getClaimAsString(StandardClaimNames.GIVEN_NAME);
        String lastName = jwt.getClaimAsString(StandardClaimNames.FAMILY_NAME);
        if (firstName != null && lastName != null) {
            return (firstName + " " + lastName).strip();
        }
        String name = jwt.getClaimAsString(StandardClaimNames.NAME);
        return name != null ? name : "";
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess == null) {
            return Collections.emptyList();
        }
        List<String> roles = (List<String>) realmAccess.get("roles");
        if (roles == null) {
            return Collections.emptyList();
        }
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toList());
    }
}
