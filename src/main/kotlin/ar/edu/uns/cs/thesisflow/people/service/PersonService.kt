package ar.edu.uns.cs.thesisflow.people.service

import ar.edu.uns.cs.thesisflow.people.dto.PersonDTO
import ar.edu.uns.cs.thesisflow.people.mapper.PersonMapper
import ar.edu.uns.cs.thesisflow.people.persistance.repository.PersonRepository
import org.springframework.stereotype.Service
import java.util.UUID
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page
import ar.edu.uns.cs.thesisflow.common.ErrorMessages
import ar.edu.uns.cs.thesisflow.common.exceptions.NotFoundException

@Service
class PersonService(
    private val personRepository: PersonRepository,
    private val personMapper: PersonMapper,
) {
    fun findAll(pageable: Pageable) = personRepository.findAll(pageable).map { personMapper.toDto(it) }

    fun findByPublicId(publicId: String?) = personMapper.toDto(findPersonByPublicId(publicId))

    fun findPersonByPublicId(publicId: String?) = publicId
        ?.let { UUID.fromString(it) }
        ?.let { personRepository.findByPublicId(it) }
        ?: throw NotFoundException(ErrorMessages.personNotFound(publicId))

    fun create(person: PersonDTO) = personMapper.toEntity(person)
        .let { personRepository.save(it) }
        .let { personMapper.toDto(it) }

    fun update(person: PersonDTO): PersonDTO {
        val existingEntity = findPersonByPublicId(person.publicId)
        personMapper.updateEntityFromDto(person, existingEntity)
        return personRepository.save(existingEntity).let { personMapper.toDto(it) }
    }
}