package ar.edu.uns.cs.thesisflow.people.service

import ar.edu.uns.cs.thesisflow.people.dto.PersonDTO
import ar.edu.uns.cs.thesisflow.people.dto.StudentDTO
import ar.edu.uns.cs.thesisflow.people.dto.toDTO
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.persistance.repository.StudentRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class StudentService(
    private val studentRepository: StudentRepository,
    private val personService: PersonService
) {
    fun findAll() = studentRepository.findAll().map { it.toDTO() }

    fun findByPublicId(publicId: String) = findEntityByPublicId(UUID.fromString(publicId)).toDTO()

    fun findEntityByPublicId(publicId: UUID) = studentRepository.findByPublicId(publicId)
        ?: throw IllegalArgumentException("No student found for publicId: $publicId")

    fun create(studentDTO: StudentDTO): StudentDTO {
        val person = studentDTO.getOrCreatePerson()
        val student = studentDTO.toEntity(person)
        return studentRepository.save(student).toDTO()
    }

    private fun StudentDTO.getOrCreatePerson() = Person(getPersonId())

    private fun StudentDTO.getPersonId() = if (personPublicId != null) {
            personService.findByPublicId(personPublicId).id!!
        } else {
            val personDTO = PersonDTO(name = name, lastname = lastname)
            personService.create(personDTO).id!!
        }

    fun update(studentDTO: StudentDTO): StudentDTO {
        val personDTO = PersonDTO(name = studentDTO.name, lastname = studentDTO.lastname)
        personService.update(personDTO)
        val student = findEntityByPublicId(UUID.fromString(studentDTO.publicId!!))
        studentDTO.update(student)
        return studentRepository.save(student).toDTO()
    }
}
