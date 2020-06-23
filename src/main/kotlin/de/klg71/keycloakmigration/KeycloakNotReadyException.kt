package de.klg71.keycloakmigration

class KeycloakNotReadyException : RuntimeException("Keycloak failed to become ready in defined timeout!")
