package de.klg71.keycloakmigration

import de.klg71.keycloakmigration.changeControl.actions.realm.AddRealmAction
import de.klg71.keycloakmigration.changeControl.actions.realm.DeleteRealmAction
import de.klg71.keycloakmigration.changeControl.actions.realm.UpdateRealmAction
import feign.slf4j.Slf4jLogger
import org.junit.After
import org.junit.Before
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

private const val adminUser = "admin"
private const val adminPass = "admin"
const val TEST_BASE_URL = "http://localhost:18080/auth"
private const val realm = "master"
private const val clientId = "admin-cli"

abstract class AbstractIntegrationTest : KoinComponent {

    protected val testRealm = "test"

    init {
        startKoin {
            modules(
                myModule(
                    adminUser, adminPass, "", TEST_BASE_URL, realm, clientId, emptyMap(),
                    failOnUndefinedVariabled = true, warnOnUndefinedVariables = true, Slf4jLogger()
                )
            )
        }
    }

    fun startKoinWithParameters(parameters: Map<String, String>) {
        startKoin {
            modules(
                myModule(
                    adminUser, adminPass, "", TEST_BASE_URL, realm, clientId, parameters,
                    failOnUndefinedVariabled = true, warnOnUndefinedVariables = true
                )
            )
        }
    }

    @Before
    fun setup() {
        AddRealmAction(testRealm).executeIt()
        try {
            UpdateRealmAction(testRealm, unmanagedAttributePolicy = "ENABLED").executeIt()
        } catch (e: Exception) {
            println("Not needed for Keycloak <24, ignore this exception")
            println("Could not update realm for unmanaged attributes: ${e.message}")
        }
    }

    @After
    fun tearDown() {
        DeleteRealmAction(testRealm).executeIt()
        stopKoin()
    }
}
