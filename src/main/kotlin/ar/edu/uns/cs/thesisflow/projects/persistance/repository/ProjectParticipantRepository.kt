package ar.edu.uns.cs.thesisflow.projects.persistance.repository

import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectParticipant
import org.springframework.data.jpa.repository.JpaRepository

interface ProjectParticipantRepository: JpaRepository<ProjectParticipant, Long>