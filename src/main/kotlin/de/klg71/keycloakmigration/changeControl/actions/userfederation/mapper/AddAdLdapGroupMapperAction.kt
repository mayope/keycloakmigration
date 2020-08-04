package de.klg71.keycloakmigration.changeControl.actions.userfederation.mapper

import de.klg71.keycloakmigration.changeControl.actions.Action
import de.klg71.keycloakmigration.keycloakapi.model.groupMapper
import de.klg71.keycloakmigration.keycloakapi.extractLocationUUID
import de.klg71.keycloakmigration.keycloakapi.userFederationByName
import java.util.UUID

class AddAdLdapGroupMapperAction(
        realm: String? = null,
        private val name: String,
        private val adName: String,
        private val groupsDn: String,
        private val groupObjectClasses: List<String> = emptyList(),
        private val groupNameLdapAttribute: String = "cn",
        private val preserveGroupInheritance: Boolean = true,
        private val membershipLdapAttribute: String = "member",
        private val membershipAttributeType: String = "DN",
        private val membershipUserLdapAttribute: String = "cn",
        private val filter: String = "",
        private val mode: String = "READ_ONLY",
        private val ignoreMissingGroups: Boolean = false,
        private val userRolesRetrieveStrategy: String = "LOAD_GROUPS_BY_MEMBER_ATTRIBUTE",
        private val mappedGroupAttributes: List<String> = emptyList(),
        private val memberofLdapAttribute: String = "memberOf",
        private val dropNonExistingGroupsDuringSync: Boolean = false,
        private val groupsPath: String = "/"

) : Action(realm) {

    private lateinit var mapperId: UUID

    override fun execute() {
        assertMapperIsCreatable(
                client, name, adName, realm())

        val userFederation = client.userFederationByName(adName, realm())
        mapperId = client.addUserFederationMapper(
                groupMapper(name, userFederation.id, groupNameLdapAttribute, groupObjectClasses, groupsDn,
                        preserveGroupInheritance, membershipLdapAttribute, membershipAttributeType,
                        membershipUserLdapAttribute, filter, mode, ignoreMissingGroups,
                        userRolesRetrieveStrategy, mappedGroupAttributes, memberofLdapAttribute,
                        dropNonExistingGroupsDuringSync, groupsPath), realm()).extractLocationUUID()
    }

    override fun undo() {
        client.deleteUserFederationMapper(realm(), mapperId)
    }

    override fun name() = "AddAdLdapGroupMapper $name"

}
