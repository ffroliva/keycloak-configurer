package io.ffroliva.keycloak.token.exchange;


import org.keycloak.representations.idm.ManagementPermissionRepresentation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.PostExchange;

import java.util.Map;

public interface KeycloakClient {

    @PostExchange(value = "/realms/{realm}/protocol/openid-connect/token", contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    Map<String, Object> getAccessToken(
            @RequestParam("client_id") String clientId,
            @RequestParam("client_secret") String clientSecret,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("grant_type") String grantType,
            @PathVariable("realm") String realm
    );

    @PostExchange(value = "/realms/{realm}/protocol/openid-connect/token", contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    Map<String, Object> tokenExchange(
            @RequestParam("client_id") String clientId,
            @RequestParam("client_secret") String clientSecret,
            @RequestParam("grant_type") String grantType,
            @RequestParam("subject_token") String subjectToken,
            @RequestParam("requested_token_type") String requestedTokenType,
            @RequestParam(value = "audience", required = false) String audience,
            @PathVariable("realm") String realm
    );

    @PostExchange(value = "/realms/{realm}/clients/{clientId}/management/permissions")
    Map<String, Object> clientSetPermissions(@PathVariable("realm") String realm,
                                             @PathVariable("clientId") String clientId,
                                             @RequestBody ManagementPermissionRepresentation status

    );
}

