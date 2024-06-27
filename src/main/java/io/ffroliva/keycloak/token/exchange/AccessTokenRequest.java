package io.ffroliva.keycloak.token.exchange;

import jakarta.validation.constraints.NotNull;

public record AccessTokenRequest(@NotNull String realm,
                                 @NotNull String clientId,
                                 @NotNull String clientSecret,
                                 @NotNull String username,
                                 @NotNull String password) {
}
