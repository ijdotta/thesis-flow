package ar.edu.uns.cs.thesisflow.projects.service

import ar.edu.uns.cs.thesisflow.catalog.persistance.repository.CareerRepository
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.persistance.repository.PersonRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.ProfessorRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.StudentCareerRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.StudentRepository
import ar.edu.uns.cs.thesisflow.projects.bulk.ProjectCsvParser
import ar.edu.uns.cs.thesisflow.projects.dto.ParticipantInfo
import ar.edu.uns.cs.thesisflow.projects.dto.ProjectDTO
import ar.edu.uns.cs.thesisflow.projects.dto.toDTO
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ParticipantRole
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.Project
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectParticipant
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ApplicationDomainRepository
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ProjectParticipantRepository
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ProjectRepository
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.TagRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Service
class ProjectService(
    private val projectRepository: ProjectRepository,
    private val applicationDomainRepository: ApplicationDomainRepository,
    private val tagRepository: TagRepository,
    private val projectParticipantRepository: ProjectParticipantRepository,
    private val studentRepository: StudentRepository,
    private val professorRepository: ProfessorRepository,
    private val personRepository: PersonRepository,
    private val careerRepository: CareerRepository,
    private val studentCareerRepository: StudentCareerRepository,
    private val projectAuthorizationService: ProjectAuthorizationService,
    private val csvParser: ProjectCsvParser,
) {
    fun findAll(pageable: Pageable): Page<ProjectDTO> =
        findAll(pageable, ProjectFilter.empty())

    fun findAll(pageable: Pageable, filter: ProjectFilter): Page<ProjectDTO> {
        val spec = ProjectSpecifications.withFilter(filter)
        return projectRepository.findAll(spec, pageable).map { it.withEnrichedParticipants() }
    }

    private fun Project.withEnrichedParticipants(): ProjectDTO {
        val participants = projectParticipantRepository.findAllByProject(this)
        val participantDTOs = participants.map { p -> p.toDTO() }
        return this.toDTO(participantDTOs)
    }

    fun findByPublicId(id: String?) = findEntityByPublicId(id).withEnrichedParticipants()

    private fun findEntityByPublicId(id: String?) =
        id?.let { projectRepository.findByPublicId(UUID.fromString(it)) }
            ?: throw IllegalArgumentException("Project not found for id $id")

    fun create(projectDTO: ProjectDTO): ProjectDTO {
        validate(projectDTO)
        val entity = projectDTO.toEntity()
        val career = careerRepository.findByPublicId(UUID.fromString(projectDTO.careerPublicId!!))
        entity.career = career
        return projectRepository.save(entity).toDTO()
    }

    private fun validate(projectDTO: ProjectDTO) {
        if (projectDTO.careerPublicId == null) {
            throw IllegalArgumentException("Career publicId is required")
        }
    }

    fun update(id: String, projectDTO: ProjectDTO): ProjectDTO {
        val entity = findEntityByPublicId(id)
        projectDTO.update(entity)
        return projectRepository.save(entity).toDTO()
    }

    fun delete(id: String) {
        val project = findEntityByPublicId(id)
        projectParticipantRepository.deleteAllByProject(project)
        projectRepository.delete(project)
    }

    @Transactional
    fun setApplicationDomain(id: String, domainId: String): ProjectDTO {
        val entity = findEntityByPublicId(id)
        projectAuthorizationService.ensureCanModify(entity)
        val domain = applicationDomainRepository.findByPublicId(UUID.fromString(domainId))
        entity.applicationDomain = domain
        return projectRepository.save(entity).toDTO()
    }

    @Transactional
    fun setTags(id: String, tagIds: List<String>): ProjectDTO {
        val entity = findEntityByPublicId(id)
        projectAuthorizationService.ensureCanModify(entity)
        val tags = tagIds.asUUIDs().let { tagRepository.findAllByPublicIdIn(it) }.toMutableSet()
        entity.tags = tags
        return projectRepository.save(entity).toDTO()
    }

    @Transactional
    fun setParticipants(id: String, participantInfos: List<ParticipantInfo>): ProjectDTO {
        val project = findEntityByPublicId(id)
        val participants = participantInfos.map { it.toProjectParticipantEntity(project) }
        val participantDTOs = projectParticipantRepository.saveAll(participants).map { it.toDTO() }
        return project.toDTO(participantDTOs)
    }

    @Transactional
    fun setCompletionDate(id: String, completionDate: java.time.LocalDate): ProjectDTO {
        val entity = findEntityByPublicId(id)
        projectAuthorizationService.ensureCanModify(entity)
        entity.completion = completionDate
        return projectRepository.save(entity).toDTO()
    }

    @Transactional
    fun setCareer(id: String, careerId: String): ProjectDTO {
        val entity = findEntityByPublicId(id)
        val career = careerRepository.findByPublicId(UUID.fromString(careerId))
            ?: throw NoSuchElementException("Career not found for publicId $careerId")
        entity.career = career
        return projectRepository.save(entity).toDTO()
    }

    private fun ParticipantInfo.toProjectParticipantEntity(project: Project): ProjectParticipant {
        val participantRole = ParticipantRole.valueOf(role)
        val person = resolvePerson(personId, participantRole, project)
        return ProjectParticipant(
            project = project,
            person = person,
            participantRole = participantRole,
        )
    }

    private fun resolvePerson(participantId: String, role: ParticipantRole, project: Project): Person {
        val publicId = UUID.fromString(participantId)
        return when (role) {
            ParticipantRole.STUDENT -> {
                val student = studentRepository.findByPublicId(publicId)
                    ?: throw NoSuchElementException("Student not found for publicId $publicId")

                // Validate that the project's career is in the student's careers
                val studentCareers = studentCareerRepository.findAllByStudent(student)
                    .mapNotNull { it.career }

                if (!studentCareers.any { it.id == project.career!!.id }) {
                    throw IllegalArgumentException(
                        "Student's careers do not include the project's career (${project.career!!.name})"
                    )
                }

                student.person ?: throw IllegalStateException("Student has no associated person")
            }
            ParticipantRole.DIRECTOR, ParticipantRole.CO_DIRECTOR -> {
                professorRepository.findByPublicId(publicId)?.person
                    ?: throw NoSuchElementException("Professor not found for publicId $publicId")
            }
            ParticipantRole.COLLABORATOR -> {
                personRepository.findByPublicId(publicId)
                    ?: throw NoSuchElementException("Person not found for publicId $publicId")
            }
        }
    }

    /**
     * Bulk import projects from a CSV file.
     * TODO: Implement CSV parsing and project creation logic
     * @param file The CSV file containing project data
     * @return A map containing the import results (e.g., success count, errors)
     */
    fun getAll(): List<Project> = projectRepository.findAll()

    @Transactional
    fun bulkImportFromCsv(file: MultipartFile): Map<String, Any> {
        val rawProjectData = csvParser.readProjectsFromCsv(file)


        // TODO: Implement bulk import logic
        // Example implementation steps:
        // 1. Parse CSV file (read headers and rows)
        // 2. Validate each row
        // 3. Create ProjectDTO instances
        // 4. Save projects using create() or save directly
        // 5. Return summary of import results

        return mapOf(
            "message" to "Bulk import not yet implemented",
            "fileName" to (file.originalFilename ?: "unknown")
        )
    }
}

private fun List<String>.asUUIDs() = this.map { UUID.fromString(it) }
