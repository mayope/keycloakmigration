package de.klg71.keycloakmigration

import de.klg71.keycloakmigration.model.AccessToken
import de.klg71.keycloakmigration.rest.KeycloakLoginClient
import org.slf4j.LoggerFactory

/**
 * Manages the keycloak access tokens and refreshes if needed
 */
class TokenHolder(private val client: KeycloakLoginClient,
                  private val adminUser: String, private val adminPassword: String,
                  private val realm: String, private val clientId: String) {
    private var tokenReceived: Long = 0L

    private var token: AccessToken = client.login(realm, "password", clientId, adminUser, adminPassword)

    companion object {
        val LOG = LoggerFactory.getLogger(TokenHolder::class.java)!!
    }

    private fun tokenExpired() = System.currentTimeMillis() - tokenReceived > token.expiresIn

    init {
        tokenReceived = System.currentTimeMillis()
    }

    fun token(): AccessToken {
        if (tokenExpired()) {
            LOG.info("Token expired retrieving new one.")
            token = client.login(realm, "password", clientId, adminUser, adminPassword)
            tokenReceived = System.currentTimeMillis()
        }
        return token
    }
}
