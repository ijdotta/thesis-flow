package ar.edu.uns.cs.thesisflow.projects.service

import ar.edu.uns.cs.thesisflow.projects.dto.ApplicationDomainDTO
import ar.edu.uns.cs.thesisflow.projects.dto.toDTO
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ApplicationDomain
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ApplicationDomainRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ApplicationDomainService(
    private val repository: ApplicationDomainRepository
) {
    fun findAll() = repository.findAll().map { it.toDTO() }

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
}