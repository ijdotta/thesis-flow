package ar.edu.uns.cs.thesisflow.projects.service

import ar.edu.uns.cs.thesisflow.projects.dto.TagDTO
import ar.edu.uns.cs.thesisflow.projects.dto.toDTO
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.Tag
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ProjectRepository
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.TagRepository
import org.springframework.stereotype.Service
import java.util.UUID
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page
import org.springframework.data.jpa.domain.Specification

@Service
class TagService(
    val tagRepository: TagRepository,
    private val projectRepository: ProjectRepository
) {
    fun findAll(pageable: Pageable): Page<TagDTO> = tagRepository.findAll(pageable).map { it.toDTO() }
    
    fun findAll(pageable: Pageable, filter: TagFilter, specification: Specification<Tag>): Page<TagDTO> =
        tagRepository.findAll(specification, pageable).map { it.toDTO() }

    fun findByPublicId(publicId: String?) = findEntityByPublicId(publicId).toDTO()

    private fun findEntityByPublicId(publicId: String?) =
        publicId?.let { tagRepository.findByPublicId(UUID.fromString(it)) }
            ?: throw IllegalArgumentException("Tag not found for id $publicId")

    fun create(tag: TagDTO): TagDTO = tagRepository.save(tag.toEntity()).toDTO()

    fun update(publicId: String?, tag: TagDTO): TagDTO {
        val entity = findEntityByPublicId(publicId)
        tag.update(entity)
        return tagRepository.save(entity).toDTO()
    }

    fun delete(id: String) {
        val entity = findEntityByPublicId(id)
        projectRepository.findAllByTagsContains(entity).forEach {
            it.tags.remove(entity)
            projectRepository.save(it)
        }
        tagRepository.delete(entity)
    }
}