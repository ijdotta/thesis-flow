package ar.edu.uns.cs.thesisflow.projects.persistance.repository

import ar.edu.uns.cs.thesisflow.projects.persistance.entity.Project
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.UUID

interface ProjectRepository: JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {
    fun findByPublicId(id: UUID): Project?
}