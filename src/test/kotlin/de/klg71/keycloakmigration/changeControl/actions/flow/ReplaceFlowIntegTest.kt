package de.klg71.keycloakmigration.changeControl.actions.flow

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.klg71.keycloakmigration.AbstractIntegrationTest
import de.klg71.keycloakmigration.changeControl.actions.MigrationException
import de.klg71.keycloakmigration.changeControl.model.ChangeSet
import de.klg71.keycloakmigration.keycloakapi.KeycloakClient
import de.klg71.keycloakmigration.keycloakapi.extractLocationUUID
import de.klg71.keycloakmigration.keycloakapi.flowSnapshot
import de.klg71.keycloakmigration.keycloakapi.model.AddFlow
import de.klg71.keycloakmigration.keycloakapi.model.AuthenticationExecutionImport
import de.klg71.keycloakmigration.keycloakapi.model.Flow
import de.klg71.keycloakmigration.keycloakapi.model.Flow.Requirement.ALTERNATIVE
import de.klg71.keycloakmigration.keycloakapi.model.Flow.Requirement.CONDITIONAL
import de.klg71.keycloakmigration.keycloakapi.model.Flow.Requirement.REQUIRED
import de.klg71.keycloakmigration.keycloakapi.model.FlowNode
import de.klg71.keycloakmigration.keycloakapi.model.UpdateFlow
import de.klg71.keycloakmigration.keycloakapi.model.addKeycloakIdentityProvider
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.util.UUID

class ReplaceFlowIntegTest : AbstractIntegrationTest() {

    val client by inject<KeycloakClient>()
    private val yamlObjectMapper by inject<ObjectMapper>(named("yamlObjectMapper"))

    private val alias = "Browser With MFA"

    private fun tree(mfaRequirement: Flow.Requirement = CONDITIONAL, withOtp: Boolean = true) = listOf(
        FlowEntry(providerId = "auth-cookie", requirement = ALTERNATIVE),
        FlowEntry(
            alias = "$alias forms", requirement = ALTERNATIVE, description = "the forms", executions = listOf(
                FlowEntry(providerId = "auth-username-password-form", requirement = REQUIRED),
                FlowEntry(
                    alias = "$alias - MFA", requirement = mfaRequirement, executions = listOfNotNull(
                        FlowEntry(providerId = "conditional-user-configured", requirement = REQUIRED),
                        FlowEntry(providerId = "webauthn-authenticator", requirement = ALTERNATIVE),
                        FlowEntry(providerId = "auth-otp-form", requirement = ALTERNATIVE).takeIf { withOtp },
                        FlowEntry(
                            providerId = "identity-provider-redirector", requirement = ALTERNATIVE,
                            config = mapOf("defaultProvider" to "some-idp")
                        )
                    )
                )
            )
        )
    )

    private fun replace(executions: List<FlowEntry> = tree()) =
        ReplaceFlowAction(testRealm, alias, "Browser login with MFA", executions = executions)

    private fun flow() = client.flows(testRealm).firstOrNull { it.alias == alias }

    private fun matches(executions: List<FlowEntry>) =
        flowTreeMatches(executions.toFlowNodes(alias), client.flowSnapshot(testRealm, alias))

    private fun executionIds() = client.flowExecutions(testRealm, alias).map { it.id }

    // A flow made the old way, with steps a reviewer can't see in one place
    private fun addOldFlow() {
        AddFlowAction(
            testRealm, alias, "old", executions = listOf(
                AuthenticationExecutionImport(UUID.randomUUID(), ALTERNATIVE, "auth-cookie", 0, 0, 10)
            )
        ).executeIt()
        AddSubFlowAction(
            testRealm, alias, "$alias forms", requirement = ALTERNATIVE, priority = 20, executions = listOf(
                AuthenticationExecutionImport(UUID.randomUUID(), REQUIRED, "auth-username-password-form", 1, 0, 10)
            )
        ).executeIt()
    }

    private fun bindEverywhere(): String {
        client.updateRealmRepresentation(testRealm, mapOf("browserFlow" to alias))
        val accountClient = client.clients(testRealm).first { it.clientId == "account" }.id.toString()
        val rep = client.clientRepresentation(testRealm, accountClient).toMutableMap()
        rep["authenticationFlowBindingOverrides"] = mapOf("browser" to flow()!!.id.toString())
        client.updateClientRepresentation(testRealm, accountClient, rep)
        client.addIdentityProvider(
            addKeycloakIdentityProvider(
                "https://idp.example.com/auth", "https://idp.example.com/token", "some-idp", "client", "secret",
                firstBrokerLoginFlowAlias = alias
            ), testRealm
        )
        return accountClient
    }

    private fun assertBoundTo(flowAlias: String, flowId: UUID, accountClient: String) {
        assertThat(client.realmRepresentation(testRealm)["browserFlow"]).isEqualTo(flowAlias)
        assertThat(client.clientRepresentation(testRealm, accountClient)["authenticationFlowBindingOverrides"])
            .isEqualTo(mapOf("browser" to flowId.toString()))
        assertThat(client.identityProviderRepresentation(testRealm, "some-idp")["firstBrokerLoginFlowAlias"])
            .isEqualTo(flowAlias)
    }

    private fun temporaryFlows() = client.flows(testRealm).filter { it.alias.contains(" replacing-") }

    @Test
    fun testCreatesMissingFlow() {
        replace().executeIt()

        assertThat(flow()!!.description).isEqualTo("Browser login with MFA")
        assertThat(matches(tree())).isTrue
        val mfa = client.flowExecutions(testRealm, alias).single { it.displayName == "$alias - MFA" }
        assertThat(mfa.requirement).isEqualTo(CONDITIONAL)
        assertThat(mfa.level).isEqualTo(1)
        val redirector = client.flowExecutions(testRealm, alias)
            .single { it.providerId == "identity-provider-redirector" }
        assertThat(client.getAuthenticatorConfiguration(testRealm, redirector.authenticationConfig!!).alias)
            .isEqualTo("$alias - MFA identity-provider-redirector")
    }

    @Test
    fun testUndoOfCreateDeletesFlow() {
        val action = replace()
        action.executeIt()
        action.undoIt()

        assertThat(flow()).isNull()
    }

    @Test
    fun testMatchingFlowIsLeftAlone() {
        replace().executeIt()
        val flowId = flow()!!.id
        val ids = executionIds()

        val action = replace()
        action.executeIt()
        action.undoIt()

        assertThat(flow()!!.id).isEqualTo(flowId)
        assertThat(executionIds()).isEqualTo(ids)
    }

    @Test
    fun testReplacesChangedFlowAndMovesBindings() {
        addOldFlow()
        val oldId = flow()!!.id
        val accountClient = bindEverywhere()

        replace().executeIt()

        val newFlow = flow()!!
        assertThat(newFlow.id).isNotEqualTo(oldId)
        assertThat(client.flows(testRealm).none { it.id == oldId }).isTrue
        assertThat(matches(tree())).isTrue
        assertThat(temporaryFlows()).isEmpty()
        assertBoundTo(alias, newFlow.id, accountClient)
    }

    @Test
    fun testReplacesNestedChange() {
        replace().executeIt()

        replace(tree(mfaRequirement = REQUIRED, withOtp = false)).executeIt()

        assertThat(matches(tree(mfaRequirement = REQUIRED, withOtp = false))).isTrue
        assertThat(client.flowExecutions(testRealm, "$alias - MFA").map { it.providerId })
            .containsExactly("conditional-user-configured", "webauthn-authenticator", "identity-provider-redirector")
    }

    @Test
    fun testUndoPutsOldFlowAndBindingsBack() {
        addOldFlow()
        val before = client.flowSnapshot(testRealm, alias)
        val accountClient = bindEverywhere()

        val action = replace()
        action.executeIt()
        action.undoIt()

        val restored = flow()!!
        assertThat(restored.description).isEqualTo("old")
        // The old flow is recreated, so only its ids are new
        assertThat(flowTreeMatches(before, client.flowSnapshot(testRealm, alias))).isTrue
        assertThat(temporaryFlows()).isEmpty()
        assertBoundTo(alias, restored.id, accountClient)
    }

    @Test
    fun testFailedBuildRollsBack() {
        addOldFlow()
        val oldId = flow()!!.id
        val before = client.flowSnapshot(testRealm, alias)
        val accountClient = bindEverywhere()

        assertThatThrownBy {
            replace(tree() + FlowEntry(providerId = "no-such-provider", requirement = REQUIRED)).executeIt()
        }

        assertThat(flow()!!.id).isEqualTo(oldId)
        assertThat(client.flowSnapshot(testRealm, alias)).isEqualTo(before)
        assertThat(temporaryFlows()).isEmpty()
        assertBoundTo(alias, oldId, accountClient)
    }

    @Test
    fun testLeftOverTemporaryFlowIsRemoved() {
        addOldFlow()
        client.addFlow(testRealm, AddFlow("$alias replacing-0123abcd", false, "", "basic-flow", true))
            .extractLocationUUID()

        replace().executeIt()

        assertThat(temporaryFlows()).isEmpty()
        assertThat(matches(tree())).isTrue
    }

    @Test
    fun testLeftOverTemporaryFlowInUseStops() {
        addOldFlow()
        val leftOver = "$alias replacing-0123abcd"
        client.addFlow(testRealm, AddFlow(leftOver, false, "", "basic-flow", true)).extractLocationUUID()
        client.updateRealmRepresentation(testRealm, mapOf("browserFlow" to leftOver))

        assertThatThrownBy { replace().executeIt() }
            .isInstanceOf(MigrationException::class.java)
            .hasMessageContaining("still in use")
        assertThat(flow()!!.description).isEqualTo("old")
    }

    @Test
    fun testMaskedConfigValueDoesNotRebuild() {
        // Keycloak masks config values the provider doesn't declare
        val executions = listOf(
            FlowEntry(providerId = "idp-confirm-link", requirement = REQUIRED, config = mapOf("custom" to "value"))
        )
        replace(executions).executeIt()
        val config = client.flowExecutions(testRealm, alias).single().authenticationConfig!!
        assertThat(client.getAuthenticatorConfiguration(testRealm, config).config["custom"]).isEqualTo("**********")
        val ids = executionIds()

        replace(executions).executeIt()

        assertThat(executionIds()).isEqualTo(ids)
    }

    @Test
    fun testBuiltInFlowIsRefused() {
        assertThatThrownBy {
            ReplaceFlowAction(testRealm, "browser", executions = tree()).executeIt()
        }.isInstanceOf(MigrationException::class.java).hasMessageContaining("built in")
    }

    @Test
    fun testInvalidTreeIsRefusedBeforeChanges() {
        addOldFlow()
        val before = client.flowSnapshot(testRealm, alias)

        assertThatThrownBy {
            replace(listOf(FlowEntry(providerId = "auth-cookie", alias = "both", requirement = REQUIRED))).executeIt()
        }.isInstanceOf(MigrationException::class.java).hasMessageContaining("either providerId")
        assertThatThrownBy {
            replace(
                listOf(
                    FlowEntry(providerId = "auth-cookie", requirement = ALTERNATIVE, priority = 10),
                    FlowEntry(providerId = "auth-otp-form", requirement = ALTERNATIVE, priority = 10)
                )
            ).executeIt()
        }.isInstanceOf(MigrationException::class.java).hasMessageContaining("Priority 10")

        assertThat(client.flowSnapshot(testRealm, alias)).isEqualTo(before)
    }

    @Test
    fun testReadFromYaml() {
        val changeSet = yamlObjectMapper.readValue<ChangeSet>(
            """
            id: replace-flow
            author: test
            realm: $testRealm
            changes:
              - replaceFlow:
                  alias: $alias
                  description: from yaml
                  executions:
                    - providerId: auth-cookie
                      requirement: ALTERNATIVE
                    - alias: $alias forms
                      requirement: ALTERNATIVE
                      executions:
                        - providerId: auth-username-password-form
                          requirement: REQUIRED
                        - providerId: identity-provider-redirector
                          requirement: ALTERNATIVE
                          configAlias: default idp
                          config:
                            defaultProvider: some-idp
            """.trimIndent()
        )

        changeSet.changes.single().executeIt()

        assertThat(flow()!!.description).isEqualTo("from yaml")
        val snapshot: List<FlowNode> = client.flowSnapshot(testRealm, alias)
        assertThat(snapshot).hasSize(2)
        val redirector = client.flowExecutions(testRealm, alias)
            .single { it.providerId == "identity-provider-redirector" }
        assertThat(client.getAuthenticatorConfiguration(testRealm, redirector.authenticationConfig!!).alias)
            .isEqualTo("default idp")
    }

    @Test
    fun testRunStoppedAfterDeletingTheOldFlowIsFinished() {
        // As a run leaves it when stopped between deleting the old flow and renaming the new one
        replace().executeIt()
        val flowId = flow()!!.id
        client.updateRealmRepresentation(testRealm, mapOf("browserFlow" to alias))
        val temporaryAlias = "$alias replacing-0123abcd"
        client.updateFlow(
            testRealm, flowId, UpdateFlow(flowId, temporaryAlias, "basic-flow", true, "Browser login with MFA")
        )
        assertThat(client.realmRepresentation(testRealm)["browserFlow"]).isEqualTo("$alias replacing-0123abcd")

        replace().executeIt()

        assertThat(flow()!!.id).isEqualTo(flowId)
        assertThat(client.realmRepresentation(testRealm)["browserFlow"]).isEqualTo(alias)
        assertThat(temporaryFlows()).isEmpty()
        assertThat(matches(tree())).isTrue
    }

    @Test
    fun testSubFlowAliasUsedElsewhereIsRefused() {
        addOldFlow()
        val before = client.flowSnapshot(testRealm, alias)
        AddFlowAction(testRealm, "Other").executeIt()
        AddSubFlowAction(testRealm, "Other", "$alias - MFA").executeIt()

        assertThatThrownBy { replace().executeIt() }
            .isInstanceOf(MigrationException::class.java)
            .hasMessageContaining("'$alias - MFA'")
            .hasMessageContaining("already used")

        assertThat(client.flowSnapshot(testRealm, alias)).isEqualTo(before)
        assertThat(temporaryFlows()).isEmpty()
    }

    @Test
    fun testOldFlowWithMaskedConfigIsRefused() {
        // Keycloak masks config keys the provider doesn't declare, so the old flow couldn't be rebuilt
        AddFlowAction(
            testRealm, alias, "old", executions = listOf(
                AuthenticationExecutionImport(
                    UUID.randomUUID(), REQUIRED, "idp-confirm-link", 0, 0, 10, mapOf("custom" to "value")
                )
            )
        ).executeIt()
        val before = client.flowSnapshot(testRealm, alias)

        assertThatThrownBy { replace().executeIt() }
            .isInstanceOf(MigrationException::class.java)
            .hasMessageContaining("can't be rolled back")

        assertThat(client.flowSnapshot(testRealm, alias)).isEqualTo(before)
    }

    @Test
    fun testEmptyConfigValueIsIgnored() {
        // Keycloak drops empty config values, so a declared one must not make the flow differ
        val executions = listOf(
            FlowEntry(
                providerId = "identity-provider-redirector", requirement = ALTERNATIVE,
                config = mapOf("defaultProvider" to "some-idp", "unused" to "")
            )
        )
        replace(executions).executeIt()
        val ids = executionIds()

        replace(executions).executeIt()

        assertThat(executionIds()).isEqualTo(ids)
    }

    @Test
    fun testBindingsBeyondTheFirstHundredIdentityProvidersAreMoved() {
        addOldFlow()
        // Keycloak lists 100 identity providers unless asked for more, so some of these are on a later page
        val identityProviders = (0..100).map { "idp-%03d".format(it) }
        identityProviders.forEach {
            client.addIdentityProvider(
                addKeycloakIdentityProvider(
                    "https://idp.example.com/auth", "https://idp.example.com/token", it, "client", "secret",
                    firstBrokerLoginFlowAlias = alias
                ), testRealm
            )
        }

        replace().executeIt()

        assertThat(matches(tree())).isTrue
        assertThat(temporaryFlows()).isEmpty()
        identityProviders.forEach {
            assertThat(client.identityProviderRepresentation(testRealm, it)["firstBrokerLoginFlowAlias"])
                .isEqualTo(alias)
        }
    }
}
