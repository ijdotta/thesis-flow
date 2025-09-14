package ar.edu.uns.cs.thesisflow.projects.persistance

import ar.edu.uns.cs.thesisflow.projects.model.Project
import org.springframework.data.jpa.repository.JpaRepository

interface ProjectRepository: JpaRepository<Project, Long>