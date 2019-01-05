package de.klg71.keycloakmigration

import org.junit.After
import org.junit.Before
import org.koin.standalone.KoinComponent
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.standalone.StandAloneContext.stopKoin


private val adminUser = "admin"
private val adminPass = "admin"
private val baseUrl = "http://localhost:8080/auth"

abstract class AbstractIntegrationTest : KoinComponent {

    @Before
    fun setup() {
        startKoin(listOf(myModule(adminUser, adminPass, baseUrl)))
    }

    @After
    fun tearDown() {
        stopKoin()
    }
}