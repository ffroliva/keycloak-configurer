package io.ffroliva.keycloak.token.exchange;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.springframework.stereotype.Service;

import java.util.Map;

@AllArgsConstructor
@Service
public class KeycloakService {

    private final KeycloakClient keycloakClient;

    public String getAccessToken(@NotNull AccessTokenRequest accessTokenRequest) {
        if (accessTokenRequest == null) {
            throw new IllegalArgumentException("Client configuration not found for: " + accessTokenRequest.clientId());
        }
        Map<String, Object> response = keycloakClient.getAccessToken(
                accessTokenRequest.clientId(),
                accessTokenRequest.clientSecret(),
                accessTokenRequest.username(),
                accessTokenRequest.password(),
                "password",
                accessTokenRequest.realm()
        );
        return (String) response.get("access_token");
    }

    public String tokenExchange(TokenExchangeRequest tokenExchangeRequest) {
        if (tokenExchangeRequest == null) {
            throw new IllegalArgumentException("TokenExchangeRequest must not be null");
        }
        Map<String, Object> response = keycloakClient.tokenExchange(
                tokenExchangeRequest.startingClientId(),
                tokenExchangeRequest.clientSecret(),
                OAuth2Constants.TOKEN_EXCHANGE_GRANT_TYPE,
                tokenExchangeRequest.subjectToken(),
                OAuth2Constants.ACCESS_TOKEN_TYPE,
                tokenExchangeRequest.audience(),
                tokenExchangeRequest.realm()
        );
        return (String) response.get("access_token");
    }
}

