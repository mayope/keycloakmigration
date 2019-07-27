package de.klg71.keycloakmigration

import de.klg71.keycloakmigration.model.AccessToken
import de.klg71.keycloakmigration.rest.KeycloakLoginClient
import java.util.*

class TokenHolder(private val client: KeycloakLoginClient,
                  adminUser: String, adminPassword: String,
                  private val realm: String, private val clientId: String) {
    var token: AccessToken = client.login(realm, "password", clientId, adminUser, adminPassword)

    val timer = Timer()

    init {
        timer.schedule(RefreshTokenTask(client, realm, token, clientId, this::callback), token.expiresIn - 1000L)
    }

    private fun callback(token: AccessToken) {
        this.token = token
        timer.schedule(RefreshTokenTask(client, realm, token, clientId, this::callback), token.expiresIn - 1000L)
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