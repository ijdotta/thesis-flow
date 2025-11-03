package ar.edu.uns.cs.thesisflow.backup.service

import ar.edu.uns.cs.thesisflow.config.JpaAuditingConfig
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestPropertySource
import java.nio.file.Files
import java.nio.file.Path

@DataJpaTest
@TestPropertySource(properties = ["spring.flyway.enabled=false"])
@Import(JpaAuditingConfig::class)
class BackupRestoreSmokeTest @Autowired constructor(
    private val entityManager: TestEntityManager,
) {
    private lateinit var backupService: BackupService

    @BeforeEach
    fun setup() {
        backupService = BackupService(
            entityManager.entityManager,
            jacksonObjectMapper().findAndRegisterModules()
        )
    }

    @Test
    fun `restoreSafeBackup runs without exceptions`() {
        val resource = requireNotNull(javaClass.getResource("/backups/sample-backup.json")) {
            "Sample backup fixture not found"
        }
        val backupJson = Files.readString(Path.of(resource.toURI()))

        assertThatCode {
            backupService.restoreBackup(backupJson)
        }.doesNotThrowAnyException()
    }
}
