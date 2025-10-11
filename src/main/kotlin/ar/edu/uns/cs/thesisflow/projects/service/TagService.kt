package ar.edu.uns.cs.thesisflow.projects.service

import ar.edu.uns.cs.thesisflow.projects.dto.TagDTO
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.TagRepository
import org.springframework.stereotype.Service
import java.util.UUID
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page
import ar.edu.uns.cs.thesisflow.common.ErrorMessages
import ar.edu.uns.cs.thesisflow.common.exceptions.NotFoundException
import ar.edu.uns.cs.thesisflow.projects.mapper.TagMapper

@Service
class TagService(
    private val tagRepository: TagRepository,
    private val tagMapper: TagMapper,
) {
    fun findAll(pageable: Pageable): Page<TagDTO> = tagRepository.findAll(pageable).map { tagMapper.toDto(it) }

    fun findByPublicId(publicId: String?) = tagMapper.toDto(findEntityByPublicId(publicId))

    private fun findEntityByPublicId(publicId: String?) =
        publicId?.let { tagRepository.findByPublicId(UUID.fromString(it)) }
            ?: throw NotFoundException(ErrorMessages.tagNotFound(publicId))

    fun create(tag: TagDTO): TagDTO = tagMapper.toEntity(tag)
        .let { tagRepository.save(it) }
        .let { tagMapper.toDto(it) }

    fun update(publicId: String?, tag: TagDTO): TagDTO {
        val entity = findEntityByPublicId(publicId)
        tagMapper.updateEntityFromDto(tag, entity)
        return tagRepository.save(entity).let { tagMapper.toDto(it) }
    }
}