package ar.edu.uns.cs.thesisflow.projects.persistance

import ar.edu.uns.cs.thesisflow.projects.model.ProjectParticipant
import org.springframework.data.jpa.repository.JpaRepository

interface ProjectParticipantRepository: JpaRepository<ProjectParticipant, Long>