package io.ffroliva.keycloak.token.exchange;

import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.representations.idm.ManagementPermissionReference;
import org.keycloak.representations.idm.ManagementPermissionRepresentation;
import org.keycloak.representations.idm.authorization.ClientPolicyRepresentation;
import org.keycloak.representations.idm.authorization.DecisionStrategy;
import org.keycloak.representations.idm.authorization.Logic;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractClientManager {

    public static final String TARGET_CLIENT_TOKEN_EXCHANGE_POLICY_NAME = "target-client-token-exchange";

    private final Keycloak keycloak;

    private final RealmName realmName;

    private final ClientId clientId;

    private final ClientResource clientResource;

    protected AbstractClientManager(Keycloak keycloak, RealmName realmName, ClientId clientId) {
        this.keycloak = keycloak;
        this.realmName = realmName;
        this.clientId = clientId;
        this.clientResource = resolveClientResource();
    }

    ClientResource getClientResource() {
        return clientResource;
    }

    private ClientResource resolveClientResource() {
        return keycloak.realm(realmName.name()).clients()
                .findByClientId(clientId.getId())
                .stream().findFirst()
                .map(r -> keycloak.realm(realmName.name()).clients().get(r.getId()))
                .orElseThrow(() -> new KeycloakAdminClientException(MessageFormat.format("{} not found.", clientId.getId())));
    }

    ManagementPermissionReference enableFineGrainedPermissions(boolean enable) {
        return clientResource.setPermissions(new ManagementPermissionRepresentation(enable));
    }

    PolicyRepresentation getTokenExchangePolicyOrElseCreate() {
        PolicyRepresentation policy = clientResource.authorization().policies().findByName(TARGET_CLIENT_TOKEN_EXCHANGE_POLICY_NAME);
        if (policy == null) {
            policy = createTokenExchangePolicy();
        }
        return policy;
    }

    // todo REMOVE
    ClientPolicyRepresentation getTokenExchangeClientPolicyOrElseCreate() {
        ClientPolicyRepresentation cpr = clientResource.authorization().policies().client().findByName(TARGET_CLIENT_TOKEN_EXCHANGE_POLICY_NAME);
        if (cpr == null) {
            cpr = createTokenExchangeClientPolicy();
        }
        return cpr;
    }

    private ClientPolicyRepresentation createTokenExchangeClientPolicy() {
        ClientPolicyRepresentation tokenExchangePolicy = new ClientPolicyRepresentation();
        tokenExchangePolicy.setName(TARGET_CLIENT_TOKEN_EXCHANGE_POLICY_NAME);
        tokenExchangePolicy.addClient(clientResource.toRepresentation().getId());
        tokenExchangePolicy.setDecisionStrategy(DecisionStrategy.UNANIMOUS);
        try (var response = clientResource.authorization().policies().client().create(tokenExchangePolicy)) {
            if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
                throw new KeycloakAdminClientException("Error creating token exchange client policy. Status code: " + response.getStatus());
            }
        }
        return clientResource.authorization().policies().client().findByName(TARGET_CLIENT_TOKEN_EXCHANGE_POLICY_NAME);
    }

    private PolicyRepresentation createTokenExchangePolicy() {
        PolicyRepresentation tokenExchangePolicy = new PolicyRepresentation();
        tokenExchangePolicy.setName(TARGET_CLIENT_TOKEN_EXCHANGE_POLICY_NAME);
        tokenExchangePolicy.setType("client");
        tokenExchangePolicy.setLogic(Logic.POSITIVE);
        tokenExchangePolicy.setDecisionStrategy(DecisionStrategy.UNANIMOUS);
        Map<String, String> config = new HashMap<>();
        config.put("clients", "[\"" + clientResource.toRepresentation().getClientId() + "\"]");
        tokenExchangePolicy.setConfig(config);

        try (var response = clientResource.authorization().policies().create(tokenExchangePolicy)) {
            if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
                throw new KeycloakAdminClientException("Error creating policy. Status code: " + response.getStatus());
            }
        }
        return clientResource.authorization().policies().findByName(TARGET_CLIENT_TOKEN_EXCHANGE_POLICY_NAME);

    }

}
