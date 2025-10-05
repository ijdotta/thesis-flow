package ar.edu.uns.cs.thesisflow.projects.service

import ar.edu.uns.cs.thesisflow.projects.dto.ApplicationDomainDTO
import ar.edu.uns.cs.thesisflow.projects.dto.toDTO
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ApplicationDomainRepository
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ProjectRepository
import org.springframework.stereotype.Service
import java.util.UUID
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page

@Service
class ApplicationDomainService(
    private val repository: ApplicationDomainRepository,
    private val projectRepository: ProjectRepository
) {
    fun findAll(pageable: Pageable): Page<ApplicationDomainDTO> = repository.findAll(pageable).map { it.toDTO() }

    fun findByPublicId(publicId: String) = findEntityByPublicId(publicId).toDTO()

    private fun findEntityByPublicId(publicId: String?) =
        publicId?.let { repository.findByPublicId(UUID.fromString(it)) }
            ?: throw IllegalArgumentException("ApplicationDomain not found for id $publicId")

    fun create(applicationDomainDTO: ApplicationDomainDTO): ApplicationDomainDTO {
        val entity = applicationDomainDTO.toEntity()
        return repository.save(entity).toDTO()
    }

    fun update(publicId: String, applicationDomainDTO: ApplicationDomainDTO): ApplicationDomainDTO {
        val entity = findEntityByPublicId(publicId)
        applicationDomainDTO.update(entity)
        return repository.save(entity).toDTO()
    }

    fun delete(id: String) {
        val entity = findEntityByPublicId(id)
        projectRepository.findFirstByApplicationDomain(entity)?.let {
            throw IllegalStateException("Cannot delete application domain $id because is associated to one or more projects")
        }
        repository.delete(entity)
    }
}