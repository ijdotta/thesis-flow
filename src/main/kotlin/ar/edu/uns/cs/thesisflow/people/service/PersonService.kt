package ar.edu.uns.cs.thesisflow.people.service

import ar.edu.uns.cs.thesisflow.people.dto.PersonDTO
import ar.edu.uns.cs.thesisflow.people.dto.toDTO
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.persistance.repository.PersonRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.ProfessorRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.StudentRepository
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ProjectParticipantRepository
import org.springframework.stereotype.Service
import java.util.UUID
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page

@Service
class PersonService(
    private val personRepository: PersonRepository,
    private val studentRepository: StudentRepository,
    private val professorRepository: ProfessorRepository,
    private val projectParticipantRepository: ProjectParticipantRepository,
) {
    fun findAll(pageable: Pageable): Page<PersonDTO> = personRepository.findAll(pageable).map { it.toDTO() }

    fun findByPublicId(publicId: String?) = findPersonByPublicId(publicId).toDTO()

    fun findPersonByPublicId(publicId: String?) = publicId?.let {
        personRepository.findByPublicId(UUID.fromString(publicId))
    } ?: throw IllegalArgumentException("Person $publicId does not exist")

    fun create(person: PersonDTO) = person.toEntity().let { personRepository.save(it) }.toDTO()

    fun update(person: PersonDTO): PersonDTO {
        val existingEntity = findPersonByPublicId(person.publicId)
        person.update(existingEntity)
        return personRepository.save(existingEntity).toDTO()
    }

    fun delete(id: String) {
        val person = findPersonByPublicId(id)
        checkNotAssociatedToAnyProjects(person, id)
        studentRepository.findFirstByPerson(person)?.let {
            studentRepository.delete(it)
        }
        professorRepository.findFirstByPerson(person)?.let {
            professorRepository.delete(it)
        }
        personRepository.delete(person)
    }

    private fun checkNotAssociatedToAnyProjects(person: Person, id: String) {
        projectParticipantRepository.findAllByPerson(person).let {
            if (it.isNotEmpty()) {
                throw IllegalStateException("Cannot delete person $id because is associated to one or more projects")
            }
        }
    }
}