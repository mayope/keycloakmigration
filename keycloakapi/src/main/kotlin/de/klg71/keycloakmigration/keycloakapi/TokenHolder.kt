package de.klg71.keycloakmigration.keycloakapi

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import de.klg71.keycloakmigration.keycloakapi.model.AccessToken
import org.slf4j.LoggerFactory
import java.awt.Desktop
import java.io.IOException
import java.lang.System.currentTimeMillis
import java.lang.System.nanoTime
import java.net.InetSocketAddress
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Manages the keycloak access tokens and refreshes if needed
 */
internal class TokenHolder(private val client: KeycloakLoginClient,
    private val adminUser: String, private val adminPassword: String,
    private val adminUseOauth: Boolean, private val adminUseOauthLocalPort: Int,
    private val baseUrl: String,
    private val realm: String, private val clientId: String,
    private val totp: String, private val tokenRefreshOffsetMs: Long = 1000) {

    companion object {
        val LOG = LoggerFactory.getLogger(TokenHolder::class.java)!!
    }

    private var token: AccessToken = initialLogin()
    private var tokenReceived: Long = currentTimeMillis()
    private var tokenReceivedNs: Long = nanoTime()

    private fun tokenExpired() =
        currentTimeMillis() - tokenReceived > TimeUnit.SECONDS.toMillis(token.expiresIn) - tokenRefreshOffsetMs

    private fun refreshExpired() =
        currentTimeMillis() - tokenReceived > TimeUnit.SECONDS.toMillis(token.refreshExpiresIn) - tokenRefreshOffsetMs

    fun token(): AccessToken {
        if (tokenExpired()) {
            LOG.info("Token expired retrieving new one.")
            token = getNewToken()
            tokenReceived = currentTimeMillis()
            tokenReceivedNs = nanoTime()
        }
        return token
    }

    private fun getNewToken() = if (!refreshExpired()) {
        LOG.info("using refreshToken")
        client.login(realm, "refresh_token", token.refreshToken, clientId)
    } else {
        LOG.info("using password")
        initialLogin()
    }

    private fun initialLogin(): AccessToken {
        return if (adminUseOauth)
            loginWithOauth()
        else
            client.login(realm, "password", clientId, adminUser, adminPassword, totp)
    }

    private fun loginWithOauth(): AccessToken {
        var server: HttpServer? = null
        try {
            lateinit var redirectedUri: URI
            val authUrl = "$baseUrl/realms/$realm/protocol/openid-connect/auth"
            val redirectUri = "http://localhost:${adminUseOauthLocalPort}/auth_callback"
            val authRequestUri =
                "$authUrl?response_type=code&client_id=$clientId&redirect_uri=$redirectUri"

            if (Desktop.isDesktopSupported()) {
                val latch = CountDownLatch(1)
                server = HttpServer.create(InetSocketAddress(adminUseOauthLocalPort), 0)

                server.createContext("/auth_callback") { exchange: HttpExchange ->
                    redirectedUri = exchange.requestURI

                    val response =
                        "Success! Authentication completed. You can close this browser tab and return to the terminal window."

                    exchange.sendResponseHeaders(200, response.length.toLong())
                    exchange.responseBody.write(response.toByteArray(StandardCharsets.UTF_8))
                    latch.countDown()
                }
                server.start()

                Desktop.getDesktop().browse(URI(authRequestUri))

                println("Attempting to automatically authorize via $authRequestUri in your default browser.")
                println("If the browser does no open, open the URL above manually.")
                val success = latch.await(2, TimeUnit.MINUTES)

                if (!success) {
                    throw RuntimeException("Authentication via default browser didn't complete in time. Aborting...")
                }
            } else {
                println("Open the following URL in your browser: $authRequestUri")
                println("Once the login has completed, enter the URL from the browser address bar here.")
                println("It should start with ${redirectUri}?... (enter everything including the text after the '?'")
                redirectedUri = URI.create(readln())
            }

            val code = redirectedUri.query
                .split("&")
                .first { it.startsWith("code=") }
                .substringAfter("=")

            val accessToken = client.login(
                realm = realm,
                grantType = "authorization_code",
                code = code,
                clientId = clientId,
                redirectUri = redirectUri
            )

            LOG.info("Success! You are now authenticated!")
            return accessToken
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        } finally {
            server?.stop(0)
        }
    }
}
