package io.ffroliva.keycloak.token.exchange;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.ScopePermissionResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ManagementPermissionReference;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;
import org.keycloak.representations.idm.authorization.ScopePermissionRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TokenExchangeService {

    private final Keycloak keycloak;

    public TokenExchangeService(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    public void configureInternalToInternalTokenExchange(RealmName realmName, OriginalClient originalClient, TargetClient targetClient) {
        DefaultClientManager originalClientManager = new DefaultClientManager(keycloak, realmName, originalClient);
        DefaultClientManager targetClientManager = new DefaultClientManager(keycloak, realmName, targetClient);
        RealmManagementClientManager realmManagerManager = new RealmManagementClientManager(keycloak, realmName);

        final RealmResource realmResource = getRealmResourceByRealName(realmName);

        // Get client resources

        ClientResource originalClientResource = originalClientManager.getClientResource();
        ClientResource targetClientResource = targetClientManager.getClientResource();

        // Enable permissions for client1
        enableClientServiceAccounts(targetClientResource);
        // Management permissions contain token-exchange permission created when feature is enabled
        ManagementPermissionReference mpr = realmManagerManager.setFineGrainedPermissions(true);

        // Enable authorization services for realmName-management client
        enableAuthorizationServices(originalClientResource);

        //create policy
        PolicyRepresentation policyRepresentation = targetClientManager.getTokenExchangePolicyOrElseCreate();

        // Update permission with created policy
        configureTokenExchangePermission(realmResource, targetClientResource, policyRepresentation, mpr);

    }

    private void enableClientServiceAccounts(ClientResource clientResource) {
        ClientRepresentation client = clientResource.toRepresentation();
        client.setServiceAccountsEnabled(true);
        clientResource.update(client);
    }

    private void enableAuthorizationServices(ClientResource clientResource) {
        ClientRepresentation client = clientResource.toRepresentation();
        client.setAuthorizationServicesEnabled(true);
        clientResource.update(client);
    }


    private void configureTokenExchangePermission(RealmResource realmResource, ClientResource targetClientResource, PolicyRepresentation policyRepresentation, ManagementPermissionReference managementPermissionReference) {

        String policyId = policyRepresentation.getId();
        String permissionId = managementPermissionReference.getScopePermissions().get("token-exchange");
        ScopePermissionResource permissionResource = targetClientResource.authorization().permissions().scope().findById(permissionId);
        ScopePermissionRepresentation permission = permissionResource.toRepresentation();

        permission.addPolicy(policyId); // update the permission with the client policy

        permissionResource.update(permission);

        final var settings = targetClientResource.authorization().getSettings();
        List<PolicyRepresentation> policies = settings.getPolicies();
        policies.add(policyRepresentation);
        settings.setPolicies(policies);
        targetClientResource.authorization().update(settings);

        var rmcm = new RealmManagementClientManager(keycloak, new RealmName(realmResource.toRepresentation().getRealm()));
        var realmManagementResource = rmcm.getClientResource();
        var realmManagementSettings = realmManagementResource.authorization().getSettings();
        realmManagementSettings.setPolicies(policies);
        realmManagementResource.authorization().update(realmManagementSettings);

    }


    RealmResource getRealmResourceByRealName(RealmName realmName) {
        var realmRepresentation = keycloak.realms().findAll().stream()
                .filter(r -> r.getRealm().equals(realmName.name()))
                .findFirst()
                .orElseThrow(() -> new KeycloakAdminClientException("Realm '%s' not found.".formatted(realmName)));
        return keycloak.realms().realm(realmRepresentation.getRealm());
    }

    String getClientUuid(RealmResource realmResource, String clientId) {
        return getClientRepresentation(realmResource, clientId).getId();
    }

    ClientResource getClientResource(RealmResource realmResource, ClientId clientId) {
        ClientRepresentation clientRepresentation = getClientRepresentation(realmResource, clientId.getId());
        return realmResource.clients().get(clientRepresentation.getId());
    }

    ClientRepresentation getClientRepresentation(RealmResource realmResource, String clientId) {
        return realmResource.clients()
                .findByClientId(clientId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Client on realm %s not found: %s".formatted(realmResource.toRepresentation().getRealm(), clientId)));
    }
}
