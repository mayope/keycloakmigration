package de.klg71.keycloakmigration.keycloakapi

import de.klg71.keycloakmigration.keycloakapi.model.AddRealm
import de.klg71.keycloakmigration.keycloakapi.model.AddUser
import de.klg71.keycloakmigration.keycloakapi.model.RealmUpdateBuilder
import de.klg71.keycloakmigration.keycloakapi.model.UpdateUserBuilder
import org.junit.Test

internal class SendEmailTest {

    // @Test
    fun testSendEmail() {
        val client = initKeycloakClient("http://localhost:18080/auth", "admin", "admin", "master", "admin-cli")
        if (client.realmNames().map { it.realm }.contains("sendMail")) {
            client.deleteRealm("sendMail")
        }
        client.addRealm(AddRealm("sendMail", true, "sendMail"))
        val realm = client.realmById("sendMail")
        val updatedRealm = RealmUpdateBuilder(realm).run {
            this.smtpServer = mapOf(

                "auth" to "true",
                "from" to "auth@mayope.net",
                "fromDisplayName" to "KeycloakApiTest (do not reply)",
                "user" to System.getenv("EMAIL_USER"),
                "password" to System.getenv("EMAIL_PASSWORD"),
                "host" to "smtp.strato.de",
                "port" to "465",
                "ssl" to "true",
                "starttls" to "true"
            )
            build()
        }
        client.updateRealm("sendMail", updatedRealm)
        client.addUser(AddUser("klg71", true, false), "sendMail")
        val user = client.userByName("klg71", "sendMail")
        val updatedUser = UpdateUserBuilder(user).run {
            email = "klg71@web.de"
            build()
        }
        client.updateUser(user.id, updatedUser, "sendMail")

        client.sendEmail(listOf(EmailActions.VERIFY_EMAIL), "sendMail", user.id)
    }
}
