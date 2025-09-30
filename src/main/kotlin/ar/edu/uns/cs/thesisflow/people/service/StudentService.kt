package ar.edu.uns.cs.thesisflow.people.service

import ar.edu.uns.cs.thesisflow.catalog.mapper.CareerMapper
import ar.edu.uns.cs.thesisflow.people.mapper.StudentMapper
import ar.edu.uns.cs.thesisflow.people.dto.StudentDTO
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.persistance.entity.StudentCareer
import ar.edu.uns.cs.thesisflow.people.persistance.repository.PersonRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.StudentCareerRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.StudentRepository
import ar.edu.uns.cs.thesisflow.catalog.persistance.entity.Career
import ar.edu.uns.cs.thesisflow.catalog.persistance.repository.CareerRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page
import java.util.*
import ar.edu.uns.cs.thesisflow.common.ErrorMessages
import ar.edu.uns.cs.thesisflow.common.exceptions.NotFoundException
import ar.edu.uns.cs.thesisflow.common.exceptions.ConflictException

@Service
class StudentService(
    private val studentRepository: StudentRepository,
    private val personRepository: PersonRepository,
    private val careerRepository: CareerRepository,
    private val studentCareerRepository: StudentCareerRepository,
    private val studentMapper: StudentMapper,
    private val careerMapper: CareerMapper,
) {
    fun findAll(pageable: Pageable): Page<StudentDTO> = studentRepository.findAll(pageable).map { studentMapper.toDto(it) }

    fun findByPublicId(publicId: String) = studentMapper.toDto(findEntityByPublicId(UUID.fromString(publicId)))

    fun findEntityByPublicId(publicId: UUID) = studentRepository.findByPublicId(publicId)
        ?: throw NotFoundException(ErrorMessages.studentNotFound(publicId))

    fun create(studentDTO: StudentDTO): StudentDTO {
        val person = studentDTO.getPerson()
        checkPersonNotAssociated(person)
        val student = studentMapper.toEntity(studentDTO, person)
        return studentRepository.save(student).let { studentMapper.toDto(it) }
    }

    private fun checkPersonNotAssociated(person: Person) {
        studentRepository.findFirstByPerson(person)?.let {
            throw ConflictException(ErrorMessages.personAlreadyStudent(person.publicId))
        }
    }

    private fun StudentDTO.getPerson() =
        personPublicId?.let { personRepository.findByPublicId(UUID.fromString(it)) }
            ?: throw NotFoundException(ErrorMessages.noPersonForStudent(personPublicId))

    fun update(studentDTO: StudentDTO): StudentDTO {
        val student = findEntityByPublicId(UUID.fromString(studentDTO.publicId!!))
        studentMapper.updateEntityFromDto(studentDTO, student)
        return studentRepository.save(student).let { studentMapper.toDto(it) }
    }

    @Transactional
    fun updateCareers(publicId: String, careers: List<String>): StudentDTO {
        val student = findEntityByPublicId(UUID.fromString(publicId))
        val resolvedCareers = getCareers(careers)
        val associations = resolvedCareers.map { StudentCareer(student = student, career = it) }
        associations.forEach { studentCareerRepository.save(it) }
        val careerDtos = resolvedCareers.map { careerMapper.toDto(it) }
        return studentMapper.toDto(student).apply { this.careers = careerDtos }
    }

    private fun getCareers(requestedCareerIds: List<String>): List<Career> {
        if (requestedCareerIds.isEmpty()) return emptyList()
        val requestedUUIDs = requestedCareerIds.map { UUID.fromString(it) }
        val existing = careerRepository.findAllByPublicIdIn(requestedUUIDs)
        val existingIds = existing.mapNotNull { it.publicId }.toSet()
        val missing = requestedUUIDs.filterNot(existingIds::contains)
        if (missing.isNotEmpty()) {
            throw NotFoundException(ErrorMessages.someCareersDoNotExist(missing))
        }
        return existing
    }
}
