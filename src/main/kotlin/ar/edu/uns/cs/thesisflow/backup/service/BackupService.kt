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

    fun createBackup(): String {
        val backup = mutableMapOf<String, List<Any>>()

        backup["career"] = getAllEntitiesByType(Career::class.java)
        backup["person"] = getAllEntitiesByType(Person::class.java)
        backup["professor"] = getAllEntitiesByType(Professor::class.java)
        backup["student"] = getAllEntitiesByType(Student::class.java)
        backup["student_career"] = getAllEntitiesByType(StudentCareer::class.java)
        backup["auth_user"] = getAllEntitiesByType(AuthUser::class.java)
        backup["professor_login_token"] = getAllEntitiesByType(ProfessorLoginToken::class.java)
        backup["application_domain"] = getAllEntitiesByType(ApplicationDomain::class.java)
        backup["tag"] = getAllEntitiesByType(Tag::class.java)
        backup["project"] = getAllEntitiesByType(Project::class.java)
        backup["project_participant"] = getAllEntitiesByType(ProjectParticipant::class.java)

        return objectMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(backup)
    }

    fun restoreBackup(backupJson: String) {
        @Suppress("UNCHECKED_CAST")
        val backup = objectMapper.readValue(backupJson, Map::class.java) as Map<String, Any>

        try {
            // Clear all data first (in reverse dependency order)
            clearAllData()

            // Restore in order of dependencies
            restoreTable(backup, "career", Career::class.java)
            restoreTable(backup, "person", Person::class.java)
            restoreTable(backup, "professor", Professor::class.java)
            restoreTable(backup, "student", Student::class.java)
            restoreTable(backup, "student_career", StudentCareer::class.java)
            restoreTable(backup, "auth_user", AuthUser::class.java)
            restoreTable(backup, "professor_login_token", ProfessorLoginToken::class.java)
            restoreTable(backup, "application_domain", ApplicationDomain::class.java)
            restoreTable(backup, "tag", Tag::class.java)
            restoreTable(backup, "project", Project::class.java)
            restoreTable(backup, "project_participant", ProjectParticipant::class.java)

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
        // Clear in reverse dependency order
        entityManager.createNativeQuery("DELETE FROM project_participant").executeUpdate()
        entityManager.createNativeQuery("DELETE FROM project").executeUpdate()
        entityManager.createNativeQuery("DELETE FROM tag").executeUpdate()
        entityManager.createNativeQuery("DELETE FROM application_domain").executeUpdate()
        entityManager.createNativeQuery("DELETE FROM professor_login_token").executeUpdate()
        entityManager.createNativeQuery("DELETE FROM auth_user").executeUpdate()
        entityManager.createNativeQuery("DELETE FROM student_career").executeUpdate()
        entityManager.createNativeQuery("DELETE FROM student").executeUpdate()
        entityManager.createNativeQuery("DELETE FROM professor").executeUpdate()
        entityManager.createNativeQuery("DELETE FROM person").executeUpdate()
        entityManager.createNativeQuery("DELETE FROM career").executeUpdate()
        entityManager.flush()
    }
}

class RestoreException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
