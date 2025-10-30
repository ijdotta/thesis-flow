package ar.edu.uns.cs.thesisflow.people.persistance.repository

import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Professor
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.UUID

interface ProfessorRepository: JpaRepository<Professor, Long>, JpaSpecificationExecutor<Professor> {
    fun findByPublicId(publicId: UUID): Professor?
    fun existsByPersonPublicId(personPublicId: UUID): Boolean
    fun findFirstByPerson(person: Person): Professor?
    fun findByEmail(email: String): Professor?
}