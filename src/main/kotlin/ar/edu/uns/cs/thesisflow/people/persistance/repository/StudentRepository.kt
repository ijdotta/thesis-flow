package ar.edu.uns.cs.thesisflow.people.persistance.repository

import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Student
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.UUID

interface StudentRepository: JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {
    fun findByPublicId(publicId: UUID): Student?
    fun findFirstByPerson(person: Person): Student?
    @Query("""
        SELECT s FROM Student s
        WHERE s.person.name LIKE :name AND s.person.lastname LIKE :lastname
    """)
    fun findFirstByNameAndLastNameLike(name: String, lastname: String): Student?
}