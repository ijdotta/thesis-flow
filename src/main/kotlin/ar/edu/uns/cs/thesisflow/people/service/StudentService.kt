package ar.edu.uns.cs.thesisflow.people.service

import ar.edu.uns.cs.thesisflow.catalog.dto.toDTO
import ar.edu.uns.cs.thesisflow.catalog.persistance.entity.Career
import ar.edu.uns.cs.thesisflow.catalog.persistance.repository.CareerRepository
import ar.edu.uns.cs.thesisflow.people.dto.StudentDTO
import ar.edu.uns.cs.thesisflow.people.dto.toDTO
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.persistance.entity.StudentCareer
import ar.edu.uns.cs.thesisflow.people.persistance.repository.PersonRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.StudentCareerRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.StudentRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page
import java.util.*
import ar.edu.uns.cs.thesisflow.common.ErrorMessages

@Service
class StudentService(
    private val studentRepository: StudentRepository,
    private val personRepository: PersonRepository,
    private val careerRepository: CareerRepository,
    private val studentCareerRepository: StudentCareerRepository,
) {
    fun findAll(pageable: Pageable): Page<StudentDTO> = studentRepository.findAll(pageable).map { it.toDTO() }

    fun findByPublicId(publicId: String) = findEntityByPublicId(UUID.fromString(publicId)).toDTO()

    fun findEntityByPublicId(publicId: UUID) = studentRepository.findByPublicId(publicId)
        ?: throw IllegalArgumentException(ErrorMessages.studentNotFound(publicId))

    fun create(studentDTO: StudentDTO): StudentDTO {
        val person = studentDTO.getPerson()
        checkPersonNotAssociated(person)
        val student = studentDTO.toEntity(person)
        return studentRepository.save(student).toDTO()
    }

    private fun checkPersonNotAssociated(person: Person) {
        studentRepository.findFirstByPerson(person)?.let {
            throw IllegalArgumentException(ErrorMessages.personAlreadyStudent(person.publicId))
        }
    }

    private fun StudentDTO.getPerson() =
        personPublicId?.let { personRepository.findByPublicId(UUID.fromString(it)) }
            ?: throw IllegalArgumentException(ErrorMessages.noPersonForStudent(personPublicId))

    fun update(studentDTO: StudentDTO): StudentDTO {
        val student = findEntityByPublicId(UUID.fromString(studentDTO.publicId!!))
        studentDTO.update(student)
        return studentRepository.save(student).toDTO()
    }

    @Transactional
    fun updateCareers(publicId: String, careers: List<String>): StudentDTO {
        val student = findEntityByPublicId(UUID.fromString(publicId))
        val resolvedCareers = getCareers(careers)
        val associations = resolvedCareers.map { StudentCareer(student = student, career = it) }
        associations.forEach { studentCareerRepository.save(it) }
        return student.toDTO(resolvedCareers.map { it.toDTO() })
    }

    private fun getCareers(requestedCareerIds: List<String>): List<Career> {
        if (requestedCareerIds.isEmpty()) return emptyList()
        val requestedUUIDs = requestedCareerIds.map { UUID.fromString(it) }
        val existing = careerRepository.findAllByPublicIdIn(requestedUUIDs)
        val existingIds = existing.mapNotNull { it.publicId }.toSet()
        val missing = requestedUUIDs.filterNot { existingIds.contains(it) }
        if (missing.isNotEmpty()) {
            throw IllegalArgumentException(ErrorMessages.someCareersDoNotExist(missing))
        }
        return existing
    }
}
