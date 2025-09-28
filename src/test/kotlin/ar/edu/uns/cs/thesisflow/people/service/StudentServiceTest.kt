package ar.edu.uns.cs.thesisflow.people.service

import ar.edu.uns.cs.thesisflow.catalog.persistance.entity.Career
import ar.edu.uns.cs.thesisflow.catalog.persistance.repository.CareerRepository
import ar.edu.uns.cs.thesisflow.people.dto.StudentDTO
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Student
import ar.edu.uns.cs.thesisflow.people.persistance.repository.PersonRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.StudentCareerRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.StudentRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import java.util.*

@DataJpaTest
class StudentServiceTest @Autowired constructor(
    private val studentRepository: StudentRepository,
    private val personRepository: PersonRepository,
    private val careerRepository: CareerRepository,
    private val studentCareerRepository: StudentCareerRepository,
) {
    private val service = StudentService(studentRepository, personRepository, careerRepository, studentCareerRepository)

    @Test
    fun `findAll returns page of DTOs`() {
        val person = personRepository.save(Person(name = "Alice", lastname = "Smith"))
        studentRepository.save(Student(person = person, studentId = "S1", email = "a@a.com"))
        val page = service.findAll(PageRequest.of(0,5))
        assertThat(page.content).hasSize(1)
        assertThat(page.content.first().studentId).isEqualTo("S1")
    }

    @Test
    fun `create fails if person missing`() {
        val dto = StudentDTO(personPublicId = UUID.randomUUID().toString(), studentId = "S2", email = "e@e.com")
        assertThatThrownBy { service.create(dto) }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `create succeeds with unique person`() {
        val person = personRepository.save(Person(name = "Bob", lastname = "White"))
        val dto = StudentDTO(personPublicId = person.publicId.toString(), studentId = "S3", email = "b@b.com")
        val created = service.create(dto)
        assertThat(created.studentId).isEqualTo("S3")
    }

    @Test
    fun `updateCareers associates careers`() {
        val person = personRepository.save(Person(name = "C", lastname = "L"))
        val student = studentRepository.save(Student(person = person, studentId = "S4", email = "c@c.com"))
        val career = careerRepository.save(Career(name = "Engineering"))
        val result = service.updateCareers(student.publicId.toString(), listOf(career.publicId.toString()))
        assertThat(result.careers).hasSize(1)
        assertThat(result.careers.first().name).isEqualTo("Engineering")
    }
}
