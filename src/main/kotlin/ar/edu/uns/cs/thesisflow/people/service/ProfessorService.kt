package ar.edu.uns.cs.thesisflow.people.service

import ar.edu.uns.cs.thesisflow.people.dto.ProfessorDTO
import ar.edu.uns.cs.thesisflow.people.persistance.repository.PersonRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.ProfessorRepository
import org.springframework.stereotype.Service
import java.util.UUID
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page
import ar.edu.uns.cs.thesisflow.common.ValidationConstants
import ar.edu.uns.cs.thesisflow.common.ErrorMessages
import ar.edu.uns.cs.thesisflow.common.exceptions.NotFoundException
import ar.edu.uns.cs.thesisflow.common.exceptions.ValidationException
import ar.edu.uns.cs.thesisflow.common.exceptions.ConflictException
import ar.edu.uns.cs.thesisflow.people.mapper.ProfessorMapper

@Service
class ProfessorService(
    private val professorRepository: ProfessorRepository,
    private val personRepository: PersonRepository,
    private val professorMapper: ProfessorMapper,
) {
    fun findAll(pageable: Pageable): Page<ProfessorDTO> = professorRepository.findAll(pageable).map { professorMapper.toDto(it) }
    fun findByPublicId(publicId: String) = professorMapper.toDto(findEntityByPublicId(publicId))

    private fun findEntityByPublicId(publicId: String?) =
        publicId?.let { professorRepository.findByPublicId(UUID.fromString(it)) }
            ?: throw NotFoundException(ErrorMessages.professorNotFound(publicId))

    fun create(professorDTO: ProfessorDTO): ProfessorDTO {
        validate(professorDTO)
        val person = professorDTO.getPerson()
        val professor = professorMapper.toEntity(professorDTO, person)
        return professorRepository.save(professor).let { professorMapper.toDto(it) }
    }

    private fun validate(professorDTO: ProfessorDTO) {
        professorDTO.personPublicId?.let { checkNotAssociated(UUID.fromString(it)) }
        professorDTO.email?.let { validateEmail(it) }
    }

    private fun ProfessorDTO.getPerson() = personPublicId?.let {
        personRepository.findByPublicId(UUID.fromString(it))
    } ?: throw NotFoundException(ErrorMessages.noPersonForProfessor(personPublicId))

    private fun checkNotAssociated(personPublicId: UUID) {
        if (professorRepository.existsByPersonPublicId(personPublicId)) {
            throw ConflictException(ErrorMessages.personAlreadyAssociated(personPublicId))
        }
    }

    private fun validateEmail(email: String?) {
        if (email.isNullOrBlank()) {
            throw ValidationException(ErrorMessages.emailNullOrBlank())
        }
        if (ValidationConstants.PROFESSOR_VALID_EMAIL_DOMAINS.none { email.endsWith(it) }) {
            throw ValidationException(ErrorMessages.emailInvalidDomain(ValidationConstants.PROFESSOR_VALID_EMAIL_DOMAINS))
        }
    }

    fun update(professorDTO: ProfessorDTO): ProfessorDTO {
        validate(professorDTO)
        val professor = findEntityByPublicId(professorDTO.publicId)
        professorDTO.personPublicId?.let {
            val person = professorDTO.getPerson()
            professor.person = person
        }
        professorMapper.updateEntityFromDto(professorDTO, professor)
        return professorRepository.save(professor).let { professorMapper.toDto(it) }
    }
}