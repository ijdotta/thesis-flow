package ar.edu.uns.cs.thesisflow.backup.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.test.context.TestPropertySource
import java.nio.file.Files
import java.nio.file.Path

@SpringBootTest
@TestPropertySource(properties = ["spring.flyway.enabled=false"])
@Import(BackupServiceIntegrationTest.MockMailConfig::class)
class BackupServiceIntegrationTest @Autowired constructor(
    private val backupService: BackupService,
) {
    private val objectMapper = jacksonObjectMapper().findAndRegisterModules()

    @Test
    fun `restore backup executes inside transaction`() {
        val resource = requireNotNull(javaClass.getResource("/backups/sample-backup.json"))
        val backupJson = Files.readString(Path.of(resource.toURI()))

        assertThatCode {
            backupService.restoreBackup(backupJson)
        }.doesNotThrowAnyException()
    }

    @TestConfiguration
    class MockMailConfig {
        @Bean
        fun javaMailSender(): JavaMailSender = Mockito.mock(JavaMailSender::class.java)
    }
}
