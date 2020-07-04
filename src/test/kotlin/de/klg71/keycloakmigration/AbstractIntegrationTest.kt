package de.klg71.keycloakmigration

import de.klg71.keycloakmigration.changeControl.actions.realm.AddRealmAction
import de.klg71.keycloakmigration.changeControl.actions.realm.DeleteRealmAction
import org.junit.After
import org.junit.Before
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

private val adminUser = "admin"
private val adminPass = "admin"
const val TEST_BASE_URL = "http://localhost:18080/auth"
private val realm = "master"
private val clientId = "admin-cli"

abstract class AbstractIntegrationTest : KoinComponent {

    protected val testRealm = "test";

    init {
        startKoin {
            modules(myModule(adminUser, adminPass, TEST_BASE_URL, realm, clientId, emptyMap(),
                    failOnUndefinedVariabled = true, warnOnUndefinedVariables = true))
        }
    }

    fun startKoinWithParameters(parameters: Map<String, String>) {
        startKoin {
            modules(myModule(adminUser, adminPass, TEST_BASE_URL, realm, clientId, parameters,
                    failOnUndefinedVariabled = true, warnOnUndefinedVariables = true))
        }
    }

    @Before
    fun setup() {
        AddRealmAction(testRealm).executeIt()
    }

    @After
    fun tearDown() {
        DeleteRealmAction(testRealm).executeIt()
        stopKoin()
    }
}
