package ar.edu.uns.cs.thesisflow.projects.persistance.repository

import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ApplicationDomain
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.Project
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.Tag
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.UUID

interface ProjectRepository: JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {
    fun findByPublicId(id: UUID): Project?
    fun findFirstByApplicationDomain(entity: ApplicationDomain): Project?
    fun findAllByTagsContains(tag: Tag): List<Project>
}