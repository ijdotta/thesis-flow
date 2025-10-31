package ar.edu.uns.cs.thesisflow.projects.persistance.repository

import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ApplicationDomain
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.UUID

interface ApplicationDomainRepository: JpaRepository<ApplicationDomain, Long>, JpaSpecificationExecutor<ApplicationDomain> {
    fun findByPublicId(publicId: UUID): ApplicationDomain?
    fun findByName(name: String): ApplicationDomain?
}