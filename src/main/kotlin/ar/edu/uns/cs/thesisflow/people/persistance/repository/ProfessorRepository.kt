package ar.edu.uns.cs.thesisflow.people.persistance.repository

import ar.edu.uns.cs.thesisflow.people.persistance.entity.Professor
import org.springframework.data.jpa.repository.JpaRepository

interface ProfessorRepository: JpaRepository<Professor, Long>