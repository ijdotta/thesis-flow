package ar.edu.uns.cs.thesisflow.people.persistance

import ar.edu.uns.cs.thesisflow.people.model.Professor
import org.springframework.data.jpa.repository.JpaRepository

interface ProfessorRepository: JpaRepository<Professor, Long>