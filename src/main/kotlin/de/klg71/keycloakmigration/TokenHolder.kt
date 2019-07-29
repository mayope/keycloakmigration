package de.klg71.keycloakmigration

import de.klg71.keycloakmigration.model.AccessToken
import de.klg71.keycloakmigration.rest.KeycloakLoginClient
import org.slf4j.LoggerFactory
import java.util.*

class TokenHolder(private val client: KeycloakLoginClient,
                  adminUser: String, adminPassword: String,
                  private val realm: String, private val clientId: String) {
    var token: AccessToken = client.login(realm, "password", clientId, adminUser, adminPassword)

    companion object {
        val LOG = LoggerFactory.getLogger(TokenHolder::class.java)!!
    }

    val timer = Timer()

    init {
        timer.schedule(RefreshTokenTask(client, realm, token, clientId, this::callback), token.expiresIn.toLong()*1000)
        LOG.info("Scheduling new token task in: ${token.expiresIn} milliseconds")
    }

    private fun callback(token: AccessToken) {
        this.token = token
        timer.schedule(RefreshTokenTask(client, realm, token, clientId, this::callback), token.expiresIn.toLong()*1000)
    }

}

private class RefreshTokenTask(private val client: KeycloakLoginClient,
                               private val realm: String,
                               private val token: AccessToken,
                               private val clientId: String,
                               private val callback: (AccessToken) -> Unit) : TimerTask() {
    override fun run() {
        client.login(realm, "request_token", token.refreshToken, clientId)
    }

}