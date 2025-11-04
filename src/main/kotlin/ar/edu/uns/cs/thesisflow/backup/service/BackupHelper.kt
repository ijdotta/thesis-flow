package ar.edu.uns.cs.thesisflow.backup.service

import ar.edu.uns.cs.thesisflow.backup.dto.*
import ar.edu.uns.cs.thesisflow.catalog.persistance.entity.Career
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Professor
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Student
import ar.edu.uns.cs.thesisflow.people.persistance.entity.StudentCareer
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ApplicationDomain
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.Project
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectParticipant
import jakarta.persistence.EntityManager

class BackupHelper(private val entityManager: EntityManager) {

    fun getAllCareerBackups(): List<CareerBackupDto> {
        val careers = getAllEntitiesByType(Career::class.java)
        return careers.map { CareerBackupDto(it.id, it.publicId, it.name, null) }
    }

    fun getAllPersonBackups(): List<PersonBackupDto> {
        val persons = getAllEntitiesByType(Person::class.java)
        return persons.map { PersonBackupDto(it.id, it.publicId, it.name, it.lastname, "") }
    }

    fun getAllProfessorBackups(): List<ProfessorBackupDto> {
        val professors = getAllEntitiesByType(Professor::class.java)
        return professors.map { 
            ProfessorBackupDto(it.id, it.publicId, it.email, it.person.publicId)
        }
    }

    fun getAllStudentBackups(): List<StudentBackupDto> {
        val students = getAllEntitiesByType(Student::class.java)
        return students.map { 
            StudentBackupDto(it.id, it.publicId, it.studentId, it.email, it.person?.publicId ?: return@map null) 
        }.filterNotNull()
    }

    fun getAllStudentCareerBackups(): List<StudentCareerBackupDto> {
        val studentCareers = getAllEntitiesByType(StudentCareer::class.java)
        return studentCareers.map { 
            StudentCareerBackupDto(it.id, it.publicId, it.student?.publicId ?: return@map null, it.career?.publicId ?: return@map null) 
        }.filterNotNull()
    }

    fun getAllApplicationDomainBackups(): List<ApplicationDomainBackupDto> {
        val domains = getAllEntitiesByType(ApplicationDomain::class.java)
        return domains.map { 
            ApplicationDomainBackupDto(it.id, it.publicId, it.name, null) 
        }
    }

    fun getAllTagBackups(): List<TagBackupDto> {
        val tags = getAllEntitiesByType(ar.edu.uns.cs.thesisflow.projects.persistance.entity.Tag::class.java)
        return tags.map { TagBackupDto(it.id, it.publicId, it.name, null) }
    }

    fun getAllProjectBackups(): List<ProjectBackupDto> {
        val projects = getAllEntitiesByType(Project::class.java)
        return projects.map { 
            ProjectBackupDto(
                it.id, 
                it.publicId, 
                it.title, 
                null,
                it.type.name,
                it.completion,
                it.career?.publicId,
                it.applicationDomain?.publicId,
                it.resources
            ) 
        }
    }

    fun getAllProjectParticipantBackups(): List<ProjectParticipantBackupDto> {
        val participants = getAllEntitiesByType(ProjectParticipant::class.java)
        return participants.map { 
            ProjectParticipantBackupDto(
                it.id, 
                it.publicId, 
                it.project.publicId, 
                it.person.publicId, 
                it.participantRole.name
            ) 
        }
    }

    private fun <T : Any> getAllEntitiesByType(entityClass: Class<T>): List<T> {
        val query = entityManager.createQuery("SELECT e FROM ${entityClass.simpleName} e", entityClass)
        return query.resultList
    }
}
