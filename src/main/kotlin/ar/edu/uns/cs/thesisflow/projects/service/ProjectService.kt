package ar.edu.uns.cs.thesisflow.projects.service

import ar.edu.uns.cs.thesisflow.people.service.PersonService
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
import java.util.UUID

@Service
class ProjectService(
    private val projectRepository: ProjectRepository,
    private val applicationDomainRepository: ApplicationDomainRepository,
    private val tagRepository: TagRepository,
    private val projectParticipantRepository: ProjectParticipantRepository,
    private val personService: PersonService,
) {
    fun findAll(pageable: Pageable): Page<ProjectDTO> {
        return projectRepository.findAll(pageable).map { it.withEnrichedParticipants() }
    }

    private fun Project.withEnrichedParticipants(): ProjectDTO {
        val participants = projectParticipantRepository.findAllByProject(this)
        val participantDTOs = participants.map { p -> p.toDTO() }
        return this.toDTO(participantDTOs)
    }

    fun findByPublicId(id: String?) = findEntityByPublicId(id).toDTO()

    private fun findEntityByPublicId(id: String?) =
        id?.let { projectRepository.findByPublicId(UUID.fromString(it)) }
            ?: throw IllegalArgumentException("Project not found for id $id")

    fun create(projectDTO: ProjectDTO) = projectRepository.save(projectDTO.toEntity()).toDTO()

    fun update(id: String, projectDTO: ProjectDTO): ProjectDTO {
        val entity = findEntityByPublicId(id)
        projectDTO.update(entity)
        return projectRepository.save(entity).toDTO()
    }

    @Transactional
    fun setApplicationDomain(id: String, domainId: String): ProjectDTO {
        val entity = findEntityByPublicId(id)
        val domain = applicationDomainRepository.findByPublicId(UUID.fromString(domainId))
        entity.applicationDomain = domain
        return projectRepository.save(entity).toDTO()
    }

    @Transactional
    fun setTags(id: String, tagIds: List<String>): ProjectDTO {
        val entity = findEntityByPublicId(id)
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

    private fun ParticipantInfo.toProjectParticipantEntity(project: Project) = ProjectParticipant(
        project = project,
        person = personService.findPersonByPublicId(personId),
        participantRole = ParticipantRole.valueOf(roleName),
    )
}

private fun List<String>.asUUIDs() = this.map { UUID.fromString(it) }
