package ar.edu.uns.cs.thesisflow.projects.persistance.repository

import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ApplicationDomain
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ApplicationDomainRepository: JpaRepository<ApplicationDomain, Long> {
    fun findByPublicId(publicId: UUID): ApplicationDomain?
}