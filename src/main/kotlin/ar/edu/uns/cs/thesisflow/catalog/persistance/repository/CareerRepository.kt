package ar.edu.uns.cs.thesisflow.catalog.persistance.repository

import ar.edu.uns.cs.thesisflow.catalog.persistance.entity.Career
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CareerRepository: JpaRepository<Career, Long> {
    fun findByPublicId(publicId: UUID): Career?
}