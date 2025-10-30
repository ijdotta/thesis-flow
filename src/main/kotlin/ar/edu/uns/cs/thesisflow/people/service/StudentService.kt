package ar.edu.uns.cs.thesisflow.people.service

import ar.edu.uns.cs.thesisflow.catalog.dto.toDTO
import ar.edu.uns.cs.thesisflow.catalog.persistance.entity.Career
import ar.edu.uns.cs.thesisflow.catalog.persistance.repository.CareerRepository
import ar.edu.uns.cs.thesisflow.people.dto.StudentDTO
import ar.edu.uns.cs.thesisflow.people.dto.toDTO
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Student
import ar.edu.uns.cs.thesisflow.people.persistance.entity.StudentCareer
import ar.edu.uns.cs.thesisflow.people.persistance.repository.PersonRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.StudentCareerRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.StudentRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page
import org.springframework.data.jpa.domain.Specification
import java.util.*

@Service
class StudentService(
    private val studentRepository: StudentRepository,
    private val personRepository: PersonRepository,
    private val careerRepository: CareerRepository,
    private val studentCareerRepository: StudentCareerRepository,
) {
    fun findAll(pageable: Pageable): Page<StudentDTO> = studentRepository.findAll(pageable).map { it.withCareers() }
    
    fun findAll(pageable: Pageable, filter: StudentFilter, specification: Specification<Student>): Page<StudentDTO> =
        studentRepository.findAll(specification, pageable).map { it.withCareers() }

    fun findByPublicId(publicId: String) = findEntityByPublicId(UUID.fromString(publicId)).withCareers()

    private fun Student.withCareers(): StudentDTO {
        val careers = studentCareerRepository.findAllByStudent(this).map { it.career!!.toDTO() }
        return this.toDTO(careers)
    }

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

    @Transactional
    fun updateCareers(publicId: String, careers: List<String>): StudentDTO {
        val student = findEntityByPublicId(UUID.fromString(publicId))
        val careers = getCareers(careers)
        val studentCareersAssociation = careers.map { StudentCareer(student = student, career = it) }
        studentCareersAssociation.forEach { studentCareerRepository.save(it) }
        return student.toDTO(careers.map { it.toDTO() })
    }

    private fun getCareers(careers: List<String>): List<Career> {
        val existing = careerRepository.findAllByPublicIdIn(careers.map { UUID.fromString(it) })
        val missingCareers = existing.filter { !careers.contains(it.publicId!!.toString()) }
        if (missingCareers.isNotEmpty()) {
            throw IllegalArgumentException("Some careers do not exist: ${missingCareers.map { it.toDTO() }}")
        }
        return existing
    }
}
