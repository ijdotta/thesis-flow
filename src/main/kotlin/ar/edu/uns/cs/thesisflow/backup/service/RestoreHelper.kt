package ar.edu.uns.cs.thesisflow.backup.service

import ar.edu.uns.cs.thesisflow.auth.persistance.entity.ProfessorLoginToken
import ar.edu.uns.cs.thesisflow.backup.dto.*
import ar.edu.uns.cs.thesisflow.catalog.persistance.entity.Career
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Professor
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Student
import ar.edu.uns.cs.thesisflow.people.persistance.entity.StudentCareer
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ApplicationDomain
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.Project
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectParticipant
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectType
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ParticipantRole
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.EntityManager
import java.time.ZoneOffset
import java.util.UUID

class RestoreHelper(
    private val entityManager: EntityManager,
    private val objectMapper: ObjectMapper,
) {

    companion object {
        private const val TABLE_CAREER = "career"
        private const val TABLE_PERSON = "person"
        private const val TABLE_PROFESSOR = "professor"
        private const val TABLE_STUDENT = "student"
        private const val TABLE_STUDENT_CAREER = "student_career"
        private const val TABLE_PROFESSOR_LOGIN_TOKEN = "professor_login_token"
        private const val TABLE_APPLICATION_DOMAIN = "application_domain"
        private const val TABLE_TAG = "tag"
        private const val TABLE_PROJECT = "project"
        private const val TABLE_PROJECT_PARTICIPANT = "project_participant"

        private val DELETE_ORDER = listOf(
            TABLE_PROJECT_PARTICIPANT,
            TABLE_PROJECT,
            TABLE_TAG,
            TABLE_APPLICATION_DOMAIN,
            TABLE_PROFESSOR_LOGIN_TOKEN,
            TABLE_STUDENT_CAREER,
            TABLE_STUDENT,
            TABLE_PROFESSOR,
            TABLE_PERSON,
            TABLE_CAREER,
        )
    }

    fun restore(backup: Map<String, Any>) {
        try {
            clearAllData()

            // Restore in order of dependencies
            restoreCareers(backup)
            restorePersons(backup)
            restoreProfessors(backup)
            restoreStudents(backup)
            restoreStudentCareers(backup)
            restoreProfessorLoginTokens(backup)
            restoreApplicationDomains(backup)
            restoreTags(backup)
            restoreProjects(backup)
            restoreProjectParticipants(backup)

            entityManager.flush()
        } catch (e: Exception) {
            throw RestoreException("Failed to restore backup: ${e.message}", e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun restoreCareers(backup: Map<String, Any>) {
        val rows = backup[TABLE_CAREER] as? List<Map<String, Any>> ?: return
        for (row in rows) {
            val dto = objectMapper.convertValue(row, CareerBackupDto::class.java)
            val career = Career(dto.name)
            setEntityId(career, dto.id)
            setEntityPublicId(career, dto.publicId)
            entityManager.merge(career)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun restorePersons(backup: Map<String, Any>) {
        val rows = backup[TABLE_PERSON] as? List<Map<String, Any>> ?: return
        for (row in rows) {
            val dto = objectMapper.convertValue(row, PersonBackupDto::class.java)
            val person = Person(dto.name, dto.lastname)
            setEntityId(person, dto.id)
            setEntityPublicId(person, dto.publicId)
            entityManager.merge(person)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun restoreProfessors(backup: Map<String, Any>) {
        val rows = backup[TABLE_PROFESSOR] as? List<Map<String, Any>> ?: return
        for (row in rows) {
            val dto = objectMapper.convertValue(row, ProfessorBackupDto::class.java)
            
            val personQuery = entityManager.createQuery(
                "SELECT p FROM Person p WHERE p.publicId = :publicId", 
                Person::class.java
            )
            personQuery.setParameter("publicId", dto.personPublicId)
            val person = personQuery.singleResult
            
            val professor = Professor(person, dto.email)
            setEntityId(professor, dto.id)
            setEntityPublicId(professor, dto.publicId)
            entityManager.merge(professor)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun restoreStudents(backup: Map<String, Any>) {
        val rows = backup[TABLE_STUDENT] as? List<Map<String, Any>> ?: return
        for (row in rows) {
            val dto = objectMapper.convertValue(row, StudentBackupDto::class.java)
            
            val personQuery = entityManager.createQuery(
                "SELECT p FROM Person p WHERE p.publicId = :publicId", 
                Person::class.java
            )
            personQuery.setParameter("publicId", dto.personPublicId)
            val person = personQuery.singleResult
            
            val student = Student(person, dto.studentId, dto.email)
            setEntityId(student, dto.id)
            setEntityPublicId(student, dto.publicId)
            entityManager.merge(student)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun restoreStudentCareers(backup: Map<String, Any>) {
        val rows = backup[TABLE_STUDENT_CAREER] as? List<Map<String, Any>> ?: return
        for (row in rows) {
            val dto = objectMapper.convertValue(row, StudentCareerBackupDto::class.java)
            
            val studentQuery = entityManager.createQuery(
                "SELECT s FROM Student s WHERE s.publicId = :publicId", 
                Student::class.java
            )
            studentQuery.setParameter("publicId", dto.studentPublicId)
            val student = studentQuery.singleResult
            
            val careerQuery = entityManager.createQuery(
                "SELECT c FROM Career c WHERE c.publicId = :publicId", 
                Career::class.java
            )
            careerQuery.setParameter("publicId", dto.careerPublicId)
            val career = careerQuery.singleResult
            
            val studentCareer = StudentCareer(student, career)
            setEntityId(studentCareer, dto.id)
            setEntityPublicId(studentCareer, dto.publicId)
            entityManager.merge(studentCareer)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun restoreProfessorLoginTokens(backup: Map<String, Any>) {
        val rows = backup[TABLE_PROFESSOR_LOGIN_TOKEN] as? List<Map<String, Any>> ?: return
        for (row in rows) {
            val dto = objectMapper.convertValue(row, ProfessorLoginTokenBackupDto::class.java)
            
            val profQuery = entityManager.createQuery(
                "SELECT p FROM Professor p WHERE p.publicId = :publicId", 
                Professor::class.java
            )
            profQuery.setParameter("publicId", dto.professorPublicId)
            val professor = profQuery.singleResult
            
            val instant = dto.expiresAt.atStartOfDay().toInstant(ZoneOffset.UTC)
            val token = ProfessorLoginToken(professor, dto.tokenValue, instant)
            setEntityId(token, dto.id)
            setEntityPublicId(token, dto.publicId)
            entityManager.merge(token)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun restoreApplicationDomains(backup: Map<String, Any>) {
        val rows = backup[TABLE_APPLICATION_DOMAIN] as? List<Map<String, Any>> ?: return
        for (row in rows) {
            val dto = objectMapper.convertValue(row, ApplicationDomainBackupDto::class.java)
            val domain = ApplicationDomain(dto.name)
            setEntityId(domain, dto.id)
            setEntityPublicId(domain, dto.publicId)
            entityManager.merge(domain)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun restoreTags(backup: Map<String, Any>) {
        val rows = backup[TABLE_TAG] as? List<Map<String, Any>> ?: return
        for (row in rows) {
            val dto = objectMapper.convertValue(row, TagBackupDto::class.java)
            val tag = ar.edu.uns.cs.thesisflow.projects.persistance.entity.Tag(dto.name)
            setEntityId(tag, dto.id)
            setEntityPublicId(tag, dto.publicId)
            entityManager.merge(tag)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun restoreProjects(backup: Map<String, Any>) {
        val rows = backup[TABLE_PROJECT] as? List<Map<String, Any>> ?: return
        for (row in rows) {
            val dto = objectMapper.convertValue(row, ProjectBackupDto::class.java)
            
            val career = if (dto.careerPublicId != null) {
                val careerQuery = entityManager.createQuery(
                    "SELECT c FROM Career c WHERE c.publicId = :publicId", 
                    Career::class.java
                )
                careerQuery.setParameter("publicId", dto.careerPublicId)
                careerQuery.singleResult
            } else null
            
            val domain = if (dto.applicationDomainPublicId != null) {
                val domainQuery = entityManager.createQuery(
                    "SELECT d FROM ApplicationDomain d WHERE d.publicId = :publicId", 
                    ApplicationDomain::class.java
                )
                domainQuery.setParameter("publicId", dto.applicationDomainPublicId)
                domainQuery.singleResult
            } else null
            
            val projectType = ProjectType.valueOf(dto.type)
            val project = Project(dto.title, projectType)
            project.career = career
            project.applicationDomain = domain
            project.completion = dto.completion
            setEntityId(project, dto.id)
            setEntityPublicId(project, dto.publicId)
            entityManager.merge(project)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun restoreProjectParticipants(backup: Map<String, Any>) {
        val rows = backup[TABLE_PROJECT_PARTICIPANT] as? List<Map<String, Any>> ?: return
        for (row in rows) {
            val dto = objectMapper.convertValue(row, ProjectParticipantBackupDto::class.java)
            
            val projectQuery = entityManager.createQuery(
                "SELECT p FROM Project p WHERE p.publicId = :publicId", 
                Project::class.java
            )
            projectQuery.setParameter("publicId", dto.projectPublicId)
            val project = projectQuery.singleResult
            
            val personQuery = entityManager.createQuery(
                "SELECT p FROM Person p WHERE p.publicId = :publicId", 
                Person::class.java
            )
            personQuery.setParameter("publicId", dto.personPublicId)
            val person = personQuery.singleResult
            
            val role = ParticipantRole.valueOf(dto.participantRole)
            val participant = ProjectParticipant(project, person, role)
            setEntityId(participant, dto.id)
            setEntityPublicId(participant, dto.publicId)
            entityManager.merge(participant)
        }
    }

    private fun clearAllData() {
        for (tableName in DELETE_ORDER) {
            entityManager.createNativeQuery("DELETE FROM $tableName").executeUpdate()
        }
        entityManager.flush()
    }

    private fun setEntityId(entity: Any, id: Long?) {
        if (id == null) return
        try {
            val field = entity.javaClass.getDeclaredField("id")
            field.isAccessible = true
            field.set(entity, id)
        } catch (e: Exception) {
            // Silently fail if id cannot be set
        }
    }

    private fun setEntityPublicId(entity: Any, publicId: UUID) {
        try {
            val field = entity.javaClass.getDeclaredField("publicId")
            field.isAccessible = true
            field.set(entity, publicId)
        } catch (e: Exception) {
            // Silently fail if publicId cannot be set
        }
    }
}

class RestoreException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
