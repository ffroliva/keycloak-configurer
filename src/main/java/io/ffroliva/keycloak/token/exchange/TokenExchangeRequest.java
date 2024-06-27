package io.ffroliva.keycloak.token.exchange;

public record TokenExchangeRequest(String subjectToken,
                                   String startingClientId,
                                   String clientSecret,
                                   String audience,
                                   String realm) {
}
