package ar.edu.uns.cs.thesisflow

import ar.edu.uns.cs.thesisflow.catalog.dto.CareerDTO
import ar.edu.uns.cs.thesisflow.catalog.service.CareerService
import ar.edu.uns.cs.thesisflow.people.dto.PersonDTO
import ar.edu.uns.cs.thesisflow.people.dto.ProfessorDTO
import ar.edu.uns.cs.thesisflow.people.dto.StudentDTO
import ar.edu.uns.cs.thesisflow.people.service.PersonService
import ar.edu.uns.cs.thesisflow.people.service.ProfessorService
import ar.edu.uns.cs.thesisflow.people.service.StudentService
import ar.edu.uns.cs.thesisflow.projects.dto.ApplicationDomainDTO
import ar.edu.uns.cs.thesisflow.projects.dto.ParticipantInfo
import ar.edu.uns.cs.thesisflow.projects.dto.ProjectDTO
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ParticipantRole
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectSubType
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectType
import ar.edu.uns.cs.thesisflow.projects.service.ApplicationDomainService
import ar.edu.uns.cs.thesisflow.projects.service.ProjectService
import ar.edu.uns.cs.thesisflow.projects.service.TagService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import java.time.Instant
import kotlin.test.Test

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = [
    "spring.datasource.url=jdbc:postgresql://localhost:5432/thesis_flow",
    "spring.datasource.username=thesis_flow_owner",
    "spring.datasource.password=owner",
    "spring.jpa.hibernate.ddl-auto=update"
])
class MigrationsAsTest(
    @Autowired val personService: PersonService,
    @Autowired val studentService: StudentService,
    @Autowired val professorService: ProfessorService,
    @Autowired val careerService: CareerService,
    @Autowired val projectService: ProjectService,
    @Autowired val applicationDomainService: ApplicationDomainService,
    @Autowired val tagService: TagService,
) {
    private final val personMap: MutableMap<String, String> = mutableMapOf()

    @Test
    fun `insert ignacio dotta as student`() {
        val person = personService.create(getPerson("Ignacio", "Dotta"))
        personMap.put("ignacio", person.publicId!!)
        val student = studentService.create(StudentDTO(
            personPublicId = person.publicId,
            studentId = "126269",
            email = "ij.dotta@gmail.com",
            person = null,
            publicId = null
        ))
        val career = careerService.create(
            CareerDTO(
                name = "Ingeniería en Sistemas de Información",
            )
        )
        studentService.updateCareers(student.publicId!!, listOf(career.publicId!!))
    }

    @Test
    fun `insert professors`() {
        val lujan = personService.create(getPerson("María Luján", "Ganuza"))
        val martin = personService.create(getPerson("Martín", "Larrea"))
        val diego = personService.create(getPerson("Diego", "Orbe"))

        personMap.put("lujan", lujan.publicId!!)
        personMap.put("martin", martin.publicId!!)
        personMap.put("diego", diego.publicId!!)

        professorService.create(ProfessorDTO(
            personPublicId = lujan.publicId,
            email = "mlg@cs.uns.edu.ar",
        ))

        professorService.create(ProfessorDTO(
            personPublicId = martin.publicId,
            email = "mll@cs.uns.edu.ar",
        ))

        professorService.create(ProfessorDTO(
            personPublicId = diego.publicId,
            email = "diego.orbe@cs.uns.edu.ar",
        ))
    }

    @Test
    fun `insert project`() {
        val project = projectService.create(
            ProjectDTO(
                title = "Sistema de Gestión y Visualización de Trabajos Finales de Carrera para el DCIC",
                type = ProjectType.FINAL_PROJECT.name,
                subtype = listOf(ProjectSubType.TYPE_1.name),
                initialSubmission = Instant.now()
            )
        )

        val domain = applicationDomainService.create(
            ApplicationDomainDTO(
                name = "Visualización de datos"
            )
        )

        projectService.setApplicationDomain(project.publicId!!, domain.publicId!!)

        projectService.setParticipants(project.publicId!!, listOf(
            ParticipantInfo("ae2bfdca-a0f7-4c48-a86b-d66acfa03c21", ParticipantRole.STUDENT.name),
            ParticipantInfo("7cdd1919-fbf4-49cf-a0b6-1a2e188f43c1", ParticipantRole.DIRECTOR.name),
            ParticipantInfo("b2dc2e05-dbf1-49bd-b1bb-c2fd81742f66", ParticipantRole.DIRECTOR.name),
            ParticipantInfo("a2ea9558-fdbc-441a-9d8f-5872b791d8ec", ParticipantRole.CO_DIRECTOR.name),
        ))
    }

    private fun getPerson(name: String, lastname: String) =
        PersonDTO(
            name = name,
            lastname = lastname,
        )
}