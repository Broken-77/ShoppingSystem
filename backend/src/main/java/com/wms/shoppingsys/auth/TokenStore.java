package com.wms.shoppingsys.auth;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class TokenStore {
    private final ConcurrentMap<String, CurrentUser> tokens = new ConcurrentHashMap<>();

    public String create(CurrentUser currentUser) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, currentUser);
        return token;
    }

    public Optional<CurrentUser> get(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(tokens.get(token));
    }
}
