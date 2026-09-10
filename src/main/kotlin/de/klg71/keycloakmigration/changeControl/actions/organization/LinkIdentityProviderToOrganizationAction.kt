package de.klg71.keycloakmigration.changeControl.actions.organization

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.isSuccessful
import de.klg71.keycloakmigration.keycloakapi.organizationByAlias
import de.klg71.keycloakmigration.keycloakapi.organizationByName

class LinkIdentityProviderToOrganizationAction(
  realm: String? = null,
  private val organizationAlias: String,
  private val identityProviderAlias: String
) : Action(realm) {

  override fun execute() {
    val org = client.organizationByAlias(organizationAlias, realm())

    val response = client.linkIdentityProviderToOrganization(
      realm = realm(),
      orgId = org.id.toString(),
      identityProviderAlias = identityProviderAlias
    )

    if (!response.isSuccessful()) {
      val errorMsg = "Failed to link Identity Provider '$identityProviderAlias' " +
        "to Organization '$organizationAlias' in realm '${realm()}'. " +
        "Status: ${response.status()}, Reason: ${response.reason()}"
      throw IllegalStateException(errorMsg)
    }
  }

  override fun undo() {
    val org = client.organizationByName(organizationAlias, realm())

    val response = client.unlinkIdentityProviderFromOrganization(
      realm = realm(),
      orgId = org.id.toString(),
      alias = identityProviderAlias
    )

    if (!response.isSuccessful()) {
      val errorMsg = "Failed to unlink Identity Provider '$identityProviderAlias' " +
        "from Organization '$organizationAlias' in realm '${realm()}'. " +
        "Status: ${response.status()}, Reason: ${response.reason()}"
      throw IllegalStateException(errorMsg)
    }
  }

  override fun name() = "LinkIdentityProviderToOrganization " +
    "$identityProviderAlias to $organizationAlias"
}
