package ar.edu.uns.cs.thesisflow.people.persistance.repository

import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Student
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface StudentRepository: JpaRepository<Student, Long> {
    fun findByPublicId(publicId: UUID): Student?
    fun findFirstByPerson(person: Person): Student?
}