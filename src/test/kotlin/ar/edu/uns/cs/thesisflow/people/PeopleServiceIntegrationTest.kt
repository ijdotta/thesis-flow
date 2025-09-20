package ar.edu.uns.cs.thesisflow.people

import ar.edu.uns.cs.thesisflow.people.dto.PersonDTO
import ar.edu.uns.cs.thesisflow.people.dto.ProfessorDTO
import ar.edu.uns.cs.thesisflow.people.dto.StudentDTO
import ar.edu.uns.cs.thesisflow.people.service.PersonService
import ar.edu.uns.cs.thesisflow.people.service.ProfessorService
import ar.edu.uns.cs.thesisflow.people.service.StudentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = [
    "spring.datasource.url=jdbc:postgresql://localhost:5432/thesis_flow",
    "spring.datasource.username=thesis_flow_owner",
    "spring.datasource.password=owner",
    "spring.jpa.hibernate.ddl-auto=create-drop"
])
class PeopleServiceIntegrationTest(
    @Autowired val personService: PersonService,
    @Autowired val studentService: StudentService,
    @Autowired val professorService: ProfessorService,
) {
    @Test
    fun `create top-level person happy path`() {
        val person = getPersonDTO()

        val newPerson = personService.create(person)

        with(newPerson) {
            assertNotNull(id)
            assertNotNull(publicId)
            assertEquals(person.name, name)
            assertEquals(person.lastname, lastname)
        }
    }

    @Test
    fun `create student person happy path`() {
        val newPerson = personService.create(getPersonDTO())
        val studentDTO = StudentDTO(
            publicId = null,
            personPublicId = newPerson.publicId,
            person = null,
            studentId = "student-1",
            email = "email@domain.com"
        )

        val student = studentService.create(studentDTO)
        with(student) {
            assertNotNull(publicId)
            assertEquals(studentDTO.personPublicId, personPublicId)
            assertNotNull(person)
            assertEquals(studentDTO.studentId, studentId)
            assertEquals(studentDTO.email, email)
        }
    }

    @Test
    fun `create professor person happy path`() {
        val person = personService.create(getPersonDTO())
        val professorDTO = ProfessorDTO(
            personPublicId = person.publicId,
            email = "mail@cs.uns.edu.ar",
        )

        val professor = professorService.create(professorDTO)

        with(professor) {
            assertNotNull(id)
            assertNotNull(publicId)
            assertEquals(professorDTO.personPublicId, personPublicId)
            assertEquals(professorDTO.email, email)
        }
    }

    private fun getPersonDTO() = PersonDTO(
        name = "name",
        lastname = "lastname",
    )
}