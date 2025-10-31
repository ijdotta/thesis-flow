package ar.edu.uns.cs.thesisflow.catalog.persistance.repository

import ar.edu.uns.cs.thesisflow.catalog.persistance.entity.Career
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.UUID

interface CareerRepository: JpaRepository<Career, Long>, JpaSpecificationExecutor<Career> {
    fun findByPublicId(publicId: UUID): Career?
    fun findAllByPublicIdIn(publicIds: Collection<UUID>): List<Career>
    fun findByName(name: String): Career?
}
