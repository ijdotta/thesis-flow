package ar.edu.uns.cs.thesisflow.people.service

import ar.edu.uns.cs.thesisflow.people.dto.StudentDTO
import ar.edu.uns.cs.thesisflow.people.dto.toDTO
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.persistance.repository.PersonRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.StudentRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class StudentService(
    private val studentRepository: StudentRepository,
    private val personRepository: PersonRepository,
) {
    fun findAll() = studentRepository.findAll().map { it.toDTO() }

    fun findByPublicId(publicId: String) = findEntityByPublicId(UUID.fromString(publicId)).toDTO()

    fun findEntityByPublicId(publicId: UUID) = studentRepository.findByPublicId(publicId)
        ?: throw IllegalArgumentException("No student found for publicId: $publicId")

    fun create(studentDTO: StudentDTO): StudentDTO {
        val person = studentDTO.getPerson()
        checkPersonNotAssociated(person)
        val student = studentDTO.toEntity(person)
        return studentRepository.save(student).toDTO()
    }

    private fun checkPersonNotAssociated(person: Person) {
        studentRepository.findFirstByPerson(person)?.let {
            throw IllegalArgumentException("Person ${person.publicId} already associated to other student")
        }
    }

    private fun StudentDTO.getPerson() =
        personPublicId?.let { personRepository.findByPublicId(UUID.fromString(it)) }
            ?: throw IllegalArgumentException("No person found for publicId: $personPublicId")

    fun update(studentDTO: StudentDTO): StudentDTO {
        val student = findEntityByPublicId(UUID.fromString(studentDTO.publicId!!))
        studentDTO.update(student)
        return studentRepository.save(student).toDTO()
    }
}
