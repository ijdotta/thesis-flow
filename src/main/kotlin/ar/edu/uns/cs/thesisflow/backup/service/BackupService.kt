package ar.edu.uns.cs.thesisflow.backup.service

import ar.edu.uns.cs.thesisflow.auth.model.AuthUser
import ar.edu.uns.cs.thesisflow.auth.persistance.entity.ProfessorLoginToken
import ar.edu.uns.cs.thesisflow.catalog.persistance.entity.Career
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Professor
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Student
import ar.edu.uns.cs.thesisflow.people.persistance.entity.StudentCareer
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ApplicationDomain
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.Project
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectParticipant
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.Tag
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import jakarta.persistence.EntityManager

@Service
class BackupService(
    private val entityManager: EntityManager,
    private val objectMapper: ObjectMapper,
) {

    companion object {
        private const val TABLE_CAREER = "career"
        private const val TABLE_PERSON = "person"
        private const val TABLE_PROFESSOR = "professor"
        private const val TABLE_STUDENT = "student"
        private const val TABLE_STUDENT_CAREER = "student_career"
        private const val TABLE_AUTH_USER = "auth_user"
        private const val TABLE_PROFESSOR_LOGIN_TOKEN = "professor_login_token"
        private const val TABLE_APPLICATION_DOMAIN = "application_domain"
        private const val TABLE_TAG = "tag"
        private const val TABLE_PROJECT = "project"
        private const val TABLE_PROJECT_PARTICIPANT = "project_participant"

        private val TABLE_CONFIGS = listOf(
            TableConfig(TABLE_CAREER, Career::class.java),
            TableConfig(TABLE_PERSON, Person::class.java),
            TableConfig(TABLE_PROFESSOR, Professor::class.java),
            TableConfig(TABLE_STUDENT, Student::class.java),
            TableConfig(TABLE_STUDENT_CAREER, StudentCareer::class.java),
            TableConfig(TABLE_AUTH_USER, AuthUser::class.java),
            TableConfig(TABLE_PROFESSOR_LOGIN_TOKEN, ProfessorLoginToken::class.java),
            TableConfig(TABLE_APPLICATION_DOMAIN, ApplicationDomain::class.java),
            TableConfig(TABLE_TAG, Tag::class.java),
            TableConfig(TABLE_PROJECT, Project::class.java),
            TableConfig(TABLE_PROJECT_PARTICIPANT, ProjectParticipant::class.java),
        )

        private val DELETE_ORDER = listOf(
            TABLE_PROJECT_PARTICIPANT,
            TABLE_PROJECT,
            TABLE_TAG,
            TABLE_APPLICATION_DOMAIN,
            TABLE_PROFESSOR_LOGIN_TOKEN,
            TABLE_AUTH_USER,
            TABLE_STUDENT_CAREER,
            TABLE_STUDENT,
            TABLE_PROFESSOR,
            TABLE_PERSON,
            TABLE_CAREER,
        )
    }

    fun createBackup(): String {
        val backup = mutableMapOf<String, List<Any>>()

        for (config in TABLE_CONFIGS) {
            backup[config.tableName] = getAllEntitiesByType(config.entityClass)
        }

        return objectMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(backup)
    }

    fun restoreBackup(backupJson: String) {
        @Suppress("UNCHECKED_CAST")
        val backup = objectMapper.readValue(backupJson, Map::class.java) as Map<String, Any>

        try {
            clearAllData()

            for (config in TABLE_CONFIGS) {
                restoreTable(backup, config.tableName, config.entityClass)
            }

            entityManager.flush()
        } catch (e: Exception) {
            throw RestoreException("Failed to restore backup: ${e.message}", e)
        }
    }

    private fun <T : Any> getAllEntitiesByType(entityClass: Class<T>): List<T> {
        val query = entityManager.createQuery("SELECT e FROM ${entityClass.simpleName} e", entityClass)
        return query.resultList
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> restoreTable(backup: Map<String, Any>, tableName: String, entityClass: Class<T>) {
        val rows = backup[tableName] as? List<Map<String, Any>> ?: return

        for (row in rows) {
            val entity = objectMapper.convertValue(row, entityClass)
            entityManager.merge(entity)
        }
    }

    private fun clearAllData() {
        for (tableName in DELETE_ORDER) {
            entityManager.createNativeQuery("DELETE FROM $tableName").executeUpdate()
        }
        entityManager.flush()
    }
}

private data class TableConfig(val tableName: String, val entityClass: Class<*>)

class RestoreException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

