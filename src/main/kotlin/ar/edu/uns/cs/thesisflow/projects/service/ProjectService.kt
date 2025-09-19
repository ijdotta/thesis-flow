package ar.edu.uns.cs.thesisflow.projects.service

import ar.edu.uns.cs.thesisflow.projects.dto.ProjectDTO
import ar.edu.uns.cs.thesisflow.projects.dto.toDTO
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ApplicationDomainRepository
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ProjectRepository
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.TagRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ProjectService(
    private val projectRepository: ProjectRepository,
    private val applicationDomainRepository: ApplicationDomainRepository,
    private val tagRepository: TagRepository,
) {
    fun findAll() = projectRepository.findAll().map { it.toDTO() }

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

    fun setApplicationDomain(id: String, domainId: String): ProjectDTO {
        val entity = findEntityByPublicId(id)
        val domain = applicationDomainRepository.findByPublicId(UUID.fromString(domainId))
        entity.applicationDomain = domain
        return projectRepository.save(entity).toDTO()
    }

    fun setTags(id: String, tagIds: List<String>): ProjectDTO {
        val entity = findEntityByPublicId(id)
        val tags = tagIds.asUUIDs().let { tagRepository.findAllByPublicIdIn(it) }.toMutableSet()
        entity.tags = tags
        return projectRepository.save(entity).toDTO()
    }
}

private fun List<String>.asUUIDs() = this.map { UUID.fromString(it) }
