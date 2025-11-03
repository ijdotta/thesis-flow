package ar.edu.uns.cs.thesisflow.backup.service

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class BackupService(
    private val entityManager: EntityManager,
    private val objectMapper: ObjectMapper,
) {
    private val backupHelper = BackupHelper(entityManager)
    private val restoreHelper = RestoreHelper(entityManager, objectMapper)

    fun createBackup(): String {
        val backup = mutableMapOf<String, List<Any>>()

        backup["career"] = backupHelper.getAllCareerBackups()
        backup["person"] = backupHelper.getAllPersonBackups()
        backup["professor"] = backupHelper.getAllProfessorBackups()
        backup["student"] = backupHelper.getAllStudentBackups()
        backup["student_career"] = backupHelper.getAllStudentCareerBackups()
        backup["application_domain"] = backupHelper.getAllApplicationDomainBackups()
        backup["tag"] = backupHelper.getAllTagBackups()
        backup["project"] = backupHelper.getAllProjectBackups()
        backup["project_participant"] = backupHelper.getAllProjectParticipantBackups()

        return objectMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(backup)
    }

    @Transactional
    fun restoreBackup(backupJson: String) {
        @Suppress("UNCHECKED_CAST")
        val backup = objectMapper.readValue(backupJson, Map::class.java) as Map<String, Any>
        restoreHelper.restore(backup)
    }
}
