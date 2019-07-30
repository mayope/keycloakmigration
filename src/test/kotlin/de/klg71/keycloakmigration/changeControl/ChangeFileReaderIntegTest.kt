package de.klg71.keycloakmigration.changeControl

import de.klg71.keycloakmigration.AbstractIntegrationTest
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.koin.core.inject

class ChangeFileReaderIntegTest : AbstractIntegrationTest() {

    private val fileReader = ChangeFileReader()

    @Test
    fun testWrongIndent() {
        assertThatThrownBy {
            fileReader.changes("src/test/resources/integration/wrong_indent/keycloak-changelog.yml")
        }.hasMessageContaining("Unable to parse").isInstanceOf(ParseException::class.java)
    }
}
