package de.klg71.keycloakmigration

import de.klg71.keycloakmigration.changeControl.actions.realm.AddRealmAction
import de.klg71.keycloakmigration.changeControl.actions.realm.DeleteRealmAction
import org.junit.After
import org.junit.Before
import org.koin.standalone.KoinComponent
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.standalone.StandAloneContext.stopKoin


private val adminUser = "admin"
private val adminPass = "admin"
private val baseUrl = "http://localhost:18080/auth"
private val realm = "master"
private val clientId = "admin-cli"

abstract class AbstractIntegrationTest : KoinComponent {

    protected val testRealm="test";

    @Before
    fun setup() {
        startKoin(listOf(myModule(adminUser, adminPass, baseUrl, realm, clientId)))

        AddRealmAction(testRealm).executeIt()
    }

    @After
    fun tearDown() {
        DeleteRealmAction(testRealm).executeIt()
        stopKoin()
    }
}