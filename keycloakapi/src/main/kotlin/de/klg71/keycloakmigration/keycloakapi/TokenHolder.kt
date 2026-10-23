package de.klg71.keycloakmigration.keycloakapi

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import de.klg71.keycloakmigration.keycloakapi.model.AccessToken
import feign.FeignException
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
import kotlin.jvm.optionals.getOrNull

/**
 * Manages the keycloak access tokens and refreshes if needed
 */
internal class TokenHolder(private val client: KeycloakLoginClient,
    private val adminUser: String,
    private val adminPassword: String,
    private val adminUseOauth: Boolean,
    private val adminUseOauthLocalPort: Int,
    private val baseUrl: String,
    private val realm: String,
    private val clientId: String,
    private val clientSecret: String?,
    private val totp: String,
    private val tokenRefreshOffsetMs: Long = 1000,
    private val loginWithClientCredentials: Boolean,
    private val fallbackToPasswordLogin: Boolean = false,
    private val fallbackClientId: String = DEFAULT_FALLBACK_CLIENT_ID) {

    companion object {
        val LOG = LoggerFactory.getLogger(TokenHolder::class.java)!!

        // Keycloak answers a client credentials login with 401 when the client doesn't exist, is disabled, can't use
        // service accounts or the secret is wrong. The response body is often lost on a 401, so the status decides;
        // a 400 only counts when its body names one of these errors.
        private const val CLIENT_REJECTED_STATUS = 401
        private const val BAD_REQUEST_STATUS = 400
        private val UNUSABLE_CLIENT_ERRORS = setOf("invalid_client", "unauthorized_client")
        private val OAUTH_ERROR = Regex("\"error\"\\s*:\\s*\"([^\"]+)\"")
    }

    // Declared before token, because initialLogin() runs while the token is initialized
    private var passwordFallbackActive = false
    private var loginClientId = clientId
    private var loginClientSecret = clientSecret ?: ""

    private var token: AccessToken = initialLogin()
    private var tokenReceived: Long = currentTimeMillis()
    private var tokenReceivedNs: Long = nanoTime()

    private fun tokenExpired() =
        currentTimeMillis() - tokenReceived > TimeUnit.SECONDS.toMillis(token.expiresIn) - tokenRefreshOffsetMs

    private fun refreshExpired() = token.refreshToken == null ||
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
        client.login(realm, "refresh_token", token.refreshToken!!, loginClientId, loginClientSecret)
    } else {
        LOG.info("logging in again")
        initialLogin()
    }

    private fun initialLogin(): AccessToken {

        return try {
            when {
                adminUseOauth ->
                    loginWithOauth()

                loginWithClientCredentials && !passwordFallbackActive ->
                    loginWithClientCredentials()

                else ->
                    loginWithPassword()
            }
        } catch (e: FeignException) {
            println(e.responseBody().getOrNull()?.toString())
            throw e
        }
    }

    private fun loginWithPassword() =
        client.login(realm, "password", loginClientId, loginClientSecret, adminUser, adminPassword, totp)

    private fun loginWithClientCredentials(): AccessToken = try {
        client.login(
            realm, "client_credentials", clientId,
            clientSecret ?: error("logging in with client credentials needs a clientSecret!")
        )
    } catch (e: FeignException) {
        val error = OAUTH_ERROR.find(e.contentUTF8())?.groupValues?.get(1)
        val clientRejected = e.status() == CLIENT_REJECTED_STATUS ||
            (e.status() == BAD_REQUEST_STATUS && error in UNUSABLE_CLIENT_ERRORS)
        if (!fallbackToPasswordLogin || !clientRejected) {
            throw e
        }
        LOG.warn(
            "Client credentials login with client '$clientId' failed (${e.status()} ${error ?: "no error details"}), " +
                "falling back to password login as '$adminUser' with client '$fallbackClientId'"
        )
        passwordFallbackActive = true
        loginClientId = fallbackClientId
        loginClientSecret = ""
        loginWithPassword()
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
                clientSecret = clientSecret ?: "",
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
