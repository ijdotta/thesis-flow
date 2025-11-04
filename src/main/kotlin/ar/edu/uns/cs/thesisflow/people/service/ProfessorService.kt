package ar.edu.uns.cs.thesisflow.people.service

import ar.edu.uns.cs.thesisflow.people.dto.ProfessorDTO
import ar.edu.uns.cs.thesisflow.people.dto.toDTO
import ar.edu.uns.cs.thesisflow.people.persistance.repository.PersonRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.ProfessorRepository
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Professor
import org.springframework.stereotype.Service
import java.util.UUID
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page
import org.springframework.data.jpa.domain.Specification

@Service
class ProfessorService(
    private val professorRepository: ProfessorRepository,
    private val personRepository: PersonRepository
) {
    companion object {
        val VALID_EMAIL_DOMAINS = listOf(
            "@cs.uns.edu.ar",
            "@uns.edu.ar"
        )
    }

    fun findAll(pageable: Pageable): Page<ProfessorDTO> = professorRepository.findAll(pageable).map { it.toDTO() }
    
    fun findAll(pageable: Pageable, filter: ProfessorFilter, specification: Specification<Professor>): Page<ProfessorDTO> =
        professorRepository.findAll(specification, pageable).map { it.toDTO() }
    
    fun findByPublicId(publicId: String) = findEntityByPublicId(publicId).toDTO()

    private fun findEntityByPublicId(publicId: String?) =
        publicId?.let { professorRepository.findByPublicId(UUID.fromString(it)) }
            ?: throw IllegalArgumentException("No professor found for $publicId")

    fun create(professorDTO: ProfessorDTO): ProfessorDTO {
        validate(professorDTO)
        val person = professorDTO.getPerson()
        val professor = professorDTO.toEntity(person)
        return professorRepository.save(professor).toDTO()
    }

    private fun validate(professorDTO: ProfessorDTO) {
        professorDTO.personPublicId?.let { checkNotAssociated(UUID.fromString(it)) }
        professorDTO.email?.let { validateEmail(it) }
    }

    private fun ProfessorDTO.getPerson() = personPublicId?.let {
        personRepository.findByPublicId(UUID.fromString(it))
    } ?: throw IllegalArgumentException("No person found for $personPublicId")

    private fun checkNotAssociated(personPublicId: UUID) {
        if (professorRepository.existsByPersonPublicId(personPublicId)) {
            throw IllegalArgumentException("Person $personPublicId is associated to other professor.")
        }
    }

    private fun validateEmail(email: String?) {
        if (email.isNullOrBlank()) {
            throw IllegalArgumentException("Email cannot be null or blank.")
        }
        if (VALID_EMAIL_DOMAINS.none { email.endsWith(it) }) {
            throw IllegalArgumentException("Email must end with '$VALID_EMAIL_DOMAINS'")
        }
    }

    fun update(professorDTO: ProfessorDTO): ProfessorDTO {
        validate(professorDTO)
        val professor = findEntityByPublicId(professorDTO.publicId)
        professorDTO.personPublicId?.let {
            val person = professorDTO.getPerson()
            professor.person = person
        }
        professorDTO.update(professor)
        return professorRepository.save(professor).toDTO()
    }

    fun delete(publicId: String) {
        val professor = findEntityByPublicId(publicId)
        professorRepository.delete(professor)
    }
}