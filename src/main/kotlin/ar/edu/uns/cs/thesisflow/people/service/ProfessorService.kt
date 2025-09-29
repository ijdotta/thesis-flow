package ar.edu.uns.cs.thesisflow.people.service

import ar.edu.uns.cs.thesisflow.people.dto.ProfessorDTO
import ar.edu.uns.cs.thesisflow.people.dto.toDTO
import ar.edu.uns.cs.thesisflow.people.persistance.repository.PersonRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.ProfessorRepository
import org.springframework.stereotype.Service
import java.util.UUID
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page
import ar.edu.uns.cs.thesisflow.common.ValidationConstants
import ar.edu.uns.cs.thesisflow.common.ErrorMessages

@Service
class ProfessorService(
    private val professorRepository: ProfessorRepository,
    private val personRepository: PersonRepository
) {
    fun findAll(pageable: Pageable): Page<ProfessorDTO> = professorRepository.findAll(pageable).map { it.toDTO() }
    fun findByPublicId(publicId: String) = findEntityByPublicId(publicId).toDTO()

    private fun findEntityByPublicId(publicId: String?) =
        publicId?.let { professorRepository.findByPublicId(UUID.fromString(it)) }
            ?: throw IllegalArgumentException(ErrorMessages.professorNotFound(publicId))

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
    } ?: throw IllegalArgumentException(ErrorMessages.noPersonForProfessor(personPublicId))

    private fun checkNotAssociated(personPublicId: UUID) {
        if (professorRepository.existsByPersonPublicId(personPublicId)) {
            throw IllegalArgumentException(ErrorMessages.personAlreadyAssociated(personPublicId))
        }
    }

    private fun validateEmail(email: String?) {
        if (email.isNullOrBlank()) {
            throw IllegalArgumentException(ErrorMessages.emailNullOrBlank())
        }
        if (ValidationConstants.PROFESSOR_VALID_EMAIL_DOMAINS.none { email.endsWith(it) }) {
            throw IllegalArgumentException(ErrorMessages.emailInvalidDomain(ValidationConstants.PROFESSOR_VALID_EMAIL_DOMAINS))
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
}