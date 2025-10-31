package ar.edu.uns.cs.thesisflow.projects.persistance.repository

import ar.edu.uns.cs.thesisflow.projects.persistance.entity.Tag
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.UUID

interface TagRepository: JpaRepository<Tag, Long>, JpaSpecificationExecutor<Tag> {
    fun findByPublicId(publicId: UUID): Tag?
    fun findAllByPublicIdIn(ids: Collection<UUID>): List<Tag>
    fun findByName(name: String): Tag?
}