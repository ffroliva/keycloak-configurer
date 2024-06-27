package io.ffroliva.keycloak.token.exchange;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.ManagementPermissionReference;

class ClientManagerTest extends AbstractKeycloakTestContainer {

    private final static RealmName TEST_REALM = new RealmName("test");
    private final static ClientId REALM_MANAGEMENT_CLIENT_ID = ClientId.of(TokenExchangeConstants.REALM_MANAGEMENT_CLIENT);
    private final static ClientId TARGET_CLIENT_ID = ClientId.of("target");

    @Test
    void given_realManagementClient_when_enableFineGrainedPermissions_then_shouldEnable() {
        // given
        // create realm 'test' if not already exists
        var realmResource = KeycloakHelper.getRealmOrElseCreate(keycloak.realms(), TEST_REALM);

        RealmManagementClientManager realmManagementManager = new RealmManagementClientManager(keycloak, TEST_REALM);
        // enables
        ManagementPermissionReference managementPermissionReference = realmManagementManager.setFineGrainedPermissions(true); // gives 404 error

        var realManagementResource = KeycloakHelper.getClientResource(realmResource, REALM_MANAGEMENT_CLIENT_ID);

        Assertions.assertThat(realManagementResource.getPermissions().isEnabled()).isTrue();

        Assertions.assertThat(managementPermissionReference).isNotNull();

        Assertions.assertThat(managementPermissionReference.getScopePermissions().get("token-exchange")).isNotNull();
    }

    @Test
    void given_targetClient_when_enableFineGrainedPermissions_then_shouldEnable() {
        var realmResource = KeycloakHelper.getRealmOrElseCreate(keycloak.realms(), TEST_REALM);
        var testClientRepresentation = KeycloakHelper.getClientResourceByClientIdOrElseCreate(keycloak, TEST_REALM, TARGET_CLIENT_ID, new ClientSecret("123456"));
        var testClientResource = KeycloakHelper.getClientResource(realmResource, TARGET_CLIENT_ID);

        var testClientManagementManager = new DefaultClientManager(keycloak, TEST_REALM, TARGET_CLIENT_ID);

        testClientManagementManager.setFineGrainedPermissions(false);
        Assertions.assertThat(testClientResource.getPermissions().isEnabled()).isFalse(); // assert that the permissions is false

        ManagementPermissionReference mpr = testClientManagementManager.setFineGrainedPermissions(true);
        Assertions.assertThat(testClientResource.getPermissions().isEnabled()).isTrue(); // verify that the permissions has been updated
        Assertions.assertThat(mpr).isNotNull();

    }

    @Test
    void given_targetClient_when_enableFineGrainedPermissionsTwice_then_shouldHaveNotSideEffect() {
        var realmResource = KeycloakHelper.getRealmOrElseCreate(keycloak.realms(), TEST_REALM);
        var testClientRepresentation = KeycloakHelper.getClientResourceByClientIdOrElseCreate(keycloak, TEST_REALM, TARGET_CLIENT_ID, new ClientSecret("123456"));
        var testClientResource = KeycloakHelper.getClientResource(realmResource, TARGET_CLIENT_ID);

        var testClientManagementManager = new DefaultClientManager(keycloak, TEST_REALM, TARGET_CLIENT_ID);


        ManagementPermissionReference mpr1 = testClientManagementManager.setFineGrainedPermissions(true);
        ManagementPermissionReference mpr2 = testClientManagementManager.setFineGrainedPermissions(true); // has no side effect
        Assertions.assertThat(mpr1.getResource()).isEqualTo(mpr2.getResource());
        Assertions.assertThat(mpr1.getScopePermissions().get("token-exchange")).isEqualTo(mpr2.getScopePermissions().get("token-exchange")); // should have same key

    }
}