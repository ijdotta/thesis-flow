package ar.edu.uns.cs.thesisflow.people.service

import ar.edu.uns.cs.thesisflow.people.dto.PersonDTO
import ar.edu.uns.cs.thesisflow.people.dto.toDTO
import ar.edu.uns.cs.thesisflow.people.persistance.repository.PersonRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PersonService(
    private val personRepository: PersonRepository,
) {
    fun findAll() = personRepository.findAll().map { it.toDTO() }

    fun findByPublicId(publicId: String?) = findPersonByPublicId(publicId).toDTO()

    private fun findPersonByPublicId(publicId: String?) = publicId?.let {
        personRepository.findByPublicId(UUID.fromString(publicId))
    } ?: throw IllegalArgumentException("Person $publicId does not exist")

    fun create(person: PersonDTO) = person.toEntity().let { personRepository.save(it) }.toDTO()

    fun update(person: PersonDTO): PersonDTO {
        val existingEntity = findPersonByPublicId(person.publicId)
        person.update(existingEntity)
        return personRepository.save(existingEntity).toDTO()
    }
}