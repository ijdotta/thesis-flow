package ar.edu.uns.cs.thesisflow.people.persistance.repository

import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PersonRepository: JpaRepository<Person, Long> {
    fun findByPublicId(publicId: UUID): Person?
    fun findFirstByNameLikeIgnoreCaseAndLastnameLikeIgnoreCase(name: String, lastname: String): Person?
}