package ar.edu.uns.cs.thesisflow.people.service

import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Student
import ar.edu.uns.cs.thesisflow.people.persistance.repository.PersonRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.StudentRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import jakarta.transaction.Transactional

@DataJpaTest
@Transactional
class StudentSpecificationsDataJpaTest @Autowired constructor(
    private val studentRepository: StudentRepository,
    private val personRepository: PersonRepository,
) {
    private lateinit var aliceStudent: Student
    private lateinit var bobStudent: Student

    @BeforeEach
    fun setup() {
        val alicePerson = personRepository.save(Person(name = "Alice", lastname = "Smith"))
        val bobPerson = personRepository.save(Person(name = "Bob", lastname = "Lopez"))

        aliceStudent = studentRepository.save(
            Student(person = alicePerson, studentId = "AP123", email = "alice@example.com")
        )
        bobStudent = studentRepository.save(
            Student(person = bobPerson, studentId = "BL456", email = "bob@example.org")
        )
        // Student without associated person to ensure LEFT joins do not drop rows
        studentRepository.save(Student(person = null, studentId = "ZZ999", email = "anonymous@example.net"))
    }

    @Test
    fun `filters by email substring`() {
        val filter = StudentFilter(email = "example.com")
        val page = studentRepository.findAll(StudentSpecifications.withFilter(filter), PageRequest.of(0, 10))
        assertThat(page.content).hasSize(1)
        assertThat(page.content.first().publicId).isEqualTo(aliceStudent.publicId)
    }

    @Test
    fun `filters by studentId prefix`() {
        val filter = StudentFilter(studentId = "AP1")
        val page = studentRepository.findAll(StudentSpecifications.withFilter(filter), PageRequest.of(0, 10))
        assertThat(page.content).hasSize(1)
        assertThat(page.content.first().publicId).isEqualTo(aliceStudent.publicId)
    }

    @Test
    fun `filters by lastname substring`() {
        val filter = StudentFilter(lastname = "lo")
        val page = studentRepository.findAll(StudentSpecifications.withFilter(filter), PageRequest.of(0, 10))
        assertThat(page.content).hasSize(1)
        assertThat(page.content.first().publicId).isEqualTo(bobStudent.publicId)
    }
}
