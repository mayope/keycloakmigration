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
private val baseUrl = "http://localhost:18080/auth"
private val realm = "master"
private val clientId = "admin-cli"

abstract class AbstractIntegrationTest : KoinComponent {

    protected val testRealm = "test";

    init {
        startKoin {
            modules(myModule(adminUser, adminPass, baseUrl, realm, clientId, emptyMap()))
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