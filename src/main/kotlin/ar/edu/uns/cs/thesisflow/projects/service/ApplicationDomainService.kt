package ar.edu.uns.cs.thesisflow.projects.service

import ar.edu.uns.cs.thesisflow.projects.dto.ApplicationDomainDTO
import ar.edu.uns.cs.thesisflow.projects.mapper.ApplicationDomainMapper
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ApplicationDomainRepository
import org.springframework.stereotype.Service
import java.util.UUID
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page
import ar.edu.uns.cs.thesisflow.common.ErrorMessages
import ar.edu.uns.cs.thesisflow.common.exceptions.NotFoundException

@Service
class ApplicationDomainService(
    private val repository: ApplicationDomainRepository,
    private val applicationDomainMapper: ApplicationDomainMapper,
) {
    fun findAll(pageable: Pageable): Page<ApplicationDomainDTO> = repository.findAll(pageable).map { applicationDomainMapper.toDto(it) }

    fun findByPublicId(publicId: String) = applicationDomainMapper.toDto(findEntityByPublicId(publicId))

    private fun findEntityByPublicId(publicId: String?) =
        publicId?.let { repository.findByPublicId(UUID.fromString(it)) }
            ?: throw NotFoundException(ErrorMessages.applicationDomainNotFound(publicId))

    fun create(applicationDomainDTO: ApplicationDomainDTO): ApplicationDomainDTO {
        val entity = applicationDomainMapper.toEntity(applicationDomainDTO)
        return repository.save(entity).let { applicationDomainMapper.toDto(it) }
    }

    fun update(publicId: String, applicationDomainDTO: ApplicationDomainDTO): ApplicationDomainDTO {
        val entity = findEntityByPublicId(publicId)
        applicationDomainMapper.updateEntityFromDto(applicationDomainDTO, entity)
        return repository.save(entity).let { applicationDomainMapper.toDto(it) }
    }
}