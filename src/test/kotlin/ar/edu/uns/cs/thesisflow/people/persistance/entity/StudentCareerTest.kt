package ar.edu.uns.cs.thesisflow.people.persistance.entity

import ar.edu.uns.cs.thesisflow.catalog.persistance.entity.Career
import ar.edu.uns.cs.thesisflow.catalog.persistance.repository.CareerRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.PersonRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.StudentCareerRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.StudentRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.test.context.TestPropertySource
import kotlin.test.Ignore

@Ignore
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = [
    "spring.datasource.url=jdbc:postgresql://localhost:5432/thesis_flow",
    "spring.datasource.username=thesis_flow_owner",
    "spring.datasource.password=owner",
    "spring.jpa.hibernate.ddl-auto=create-drop"
])
class StudentCareerTest(
    @Autowired val careerRepository: CareerRepository,
    @Autowired val personRepository: PersonRepository,
    @Autowired val studentRepository: StudentRepository,
    @Autowired val studentCareerRepository: StudentCareerRepository
) {
    @Test
    fun `insert happy path`() {
        val career = Career(name = "career")
        val savedCareer = careerRepository.save(career)
        val person = Person(name = "name", lastname = "lastname")
        val savedPerson = personRepository.save(person)
        val student = Student(person = savedPerson, studentId = "student-id", email = "email")
        val savedStudent = studentRepository.save(student)

        val studentCareer = StudentCareer(student = savedStudent, career = savedCareer)
        val savedStudentCareer = studentCareerRepository.save(studentCareer)

        assertNotNull(savedStudentCareer)
        assertEquals(1, studentCareerRepository.count())
        assertEquals(studentCareer.id, savedStudentCareer.id)
        assertEquals(studentCareer.student, savedStudentCareer.student)
        assertEquals(studentCareer.career, savedStudentCareer.career)
    }
}