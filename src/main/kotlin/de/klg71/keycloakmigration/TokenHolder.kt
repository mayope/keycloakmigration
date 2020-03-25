package de.klg71.keycloakmigration

import de.klg71.keycloakmigration.model.AccessToken
import de.klg71.keycloakmigration.rest.KeycloakLoginClient
import org.slf4j.LoggerFactory
import java.lang.System.currentTimeMillis
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS
import javax.xml.datatype.DatatypeConstants

/**
 * Manages the keycloak access tokens and refreshes if needed
 */
class TokenHolder(private val client: KeycloakLoginClient,
                  private val adminUser: String, private val adminPassword: String,
                  private val realm: String, private val clientId: String) {

    companion object {
        val LOG = LoggerFactory.getLogger(TokenHolder::class.java)!!
    }
    private var token: AccessToken = client.login(realm, "password", clientId, adminUser, adminPassword)
    private var tokenReceived: Long = currentTimeMillis()

    private fun tokenExpired() = currentTimeMillis() - tokenReceived > SECONDS.toMillis(token.expiresIn)

    fun token(): AccessToken {
        if (tokenExpired()) {
            LOG.info("Token expired retrieving new one.")
            token = client.login(realm, "password", clientId, adminUser, adminPassword)
            tokenReceived = currentTimeMillis()
        }
        return token
    }
}
