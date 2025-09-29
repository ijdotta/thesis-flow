package ar.edu.uns.cs.thesisflow.people

import ar.edu.uns.cs.thesisflow.catalog.dto.CareerDTO
import ar.edu.uns.cs.thesisflow.catalog.service.CareerService
import ar.edu.uns.cs.thesisflow.people.dto.PersonDTO
import ar.edu.uns.cs.thesisflow.people.dto.ProfessorDTO
import ar.edu.uns.cs.thesisflow.people.dto.StudentDTO
import ar.edu.uns.cs.thesisflow.people.service.PersonService
import ar.edu.uns.cs.thesisflow.people.service.ProfessorService
import ar.edu.uns.cs.thesisflow.people.service.StudentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest
@Transactional
class PeopleServiceIntegrationTest(
    @Autowired val personService: PersonService,
    @Autowired val studentService: StudentService,
    @Autowired val professorService: ProfessorService,
    @Autowired val careerService: CareerService,
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
            personPublicId = newPerson.publicId,
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

    @Test
    fun `add careers to students happy path`() {
        val person = personService.create(getPersonDTO())
        val studentDTO = StudentDTO(
            personPublicId = person.publicId,
            studentId = "student-1",
            email = "email@domain.com"
        )
        val student = studentService.create(studentDTO)

        val careerDTO = CareerDTO(name = "Systems Information Engineering")
        val career = careerService.create(careerDTO)

        val studentWithCareers = studentService.updateCareers(student.publicId!!, listOf(career.publicId!!))

        assertEquals(1, studentWithCareers.careers.size)
        assertEquals(career.publicId, studentWithCareers.careers[0].publicId)
    }

    private fun getPersonDTO() = PersonDTO(
        name = "name",
        lastname = "lastname",
    )
}