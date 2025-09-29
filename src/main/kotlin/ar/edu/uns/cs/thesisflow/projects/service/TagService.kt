package ar.edu.uns.cs.thesisflow.projects.service

import ar.edu.uns.cs.thesisflow.projects.dto.TagDTO
import ar.edu.uns.cs.thesisflow.projects.dto.toDTO
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.TagRepository
import org.springframework.stereotype.Service
import java.util.UUID
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page
import ar.edu.uns.cs.thesisflow.common.ErrorMessages
import ar.edu.uns.cs.thesisflow.common.exceptions.NotFoundException

@Service
class TagService(
    val tagRepository: TagRepository
) {
    fun findAll(pageable: Pageable): Page<TagDTO> = tagRepository.findAll(pageable).map { it.toDTO() }

    fun findByPublicId(publicId: String?) = findEntityByPublicId(publicId).toDTO()

    private fun findEntityByPublicId(publicId: String?) =
        publicId?.let { tagRepository.findByPublicId(UUID.fromString(it)) }
            ?: throw NotFoundException(ErrorMessages.tagNotFound(publicId))

    fun create(tag: TagDTO): TagDTO = tagRepository.save(tag.toEntity()).toDTO()

    fun update(publicId: String?, tag: TagDTO): TagDTO {
        val entity = findEntityByPublicId(publicId)
        tag.update(entity)
        return tagRepository.save(entity).toDTO()
    }
}