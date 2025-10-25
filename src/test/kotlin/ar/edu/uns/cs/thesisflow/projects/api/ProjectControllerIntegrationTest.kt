package ar.edu.uns.cs.thesisflow.projects.api

import ar.edu.uns.cs.thesisflow.catalog.persistance.entity.Career
import ar.edu.uns.cs.thesisflow.catalog.persistance.repository.CareerRepository
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.persistance.repository.PersonRepository
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.*
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ApplicationDomainRepository
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ProjectParticipantRepository
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ProjectRepository
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin", roles = ["ADMIN"])
class ProjectControllerIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val projectRepository: ProjectRepository,
    private val participantRepository: ProjectParticipantRepository,
    private val personRepository: PersonRepository,
    private val domainRepository: ApplicationDomainRepository,
    private val careerRepository: CareerRepository,
) {
    private lateinit var completed: Project
    private lateinit var inProgress: Project

    @BeforeEach
    fun initData() {
        participantRepository.deleteAll()
        projectRepository.deleteAll()
        domainRepository.deleteAll()
        personRepository.deleteAll()
        careerRepository.deleteAll()

        val domain = domainRepository.save(ApplicationDomain(name = "Distributed Systems"))
        val career = careerRepository.save(Career(name = "Career ${System.nanoTime()}"))
        completed = projectRepository.save(
            Project(
                title = "Consensus Algorithms",
                type = ProjectType.THESIS,
                subType = mutableSetOf(ProjectSubType.TYPE_1),
                applicationDomain = domain,
                completion = LocalDate.now(),
                career = career
            )
        )
        inProgress = projectRepository.save(
            Project(
                title = "Event Sourcing Platform",
                type = ProjectType.THESIS,
                subType = mutableSetOf(ProjectSubType.TYPE_1),
                applicationDomain = domain,
                completion = null,
                career = career
            )
        )
        val prof = personRepository.save(Person(name = "Elena", lastname = "Martinez"))
        val student = personRepository.save(Person(name = "Carlos", lastname = "Diaz"))
        participantRepository.save(ProjectParticipant(project = completed, person = prof, participantRole = ParticipantRole.DIRECTOR))
        participantRepository.save(ProjectParticipant(project = completed, person = student, participantRole = ParticipantRole.STUDENT))
        participantRepository.save(ProjectParticipant(project = inProgress, person = prof, participantRole = ParticipantRole.CO_DIRECTOR))
    }

    @Test
    fun `completed true returns only finished projects`() {
        mockMvc.get("/projects") {
            param("completed", "true")
        }.andExpect {
            status { isOk() }
            content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
            jsonPath("$.content[*].title", everyItem(`is`(completed.title)))
        }
    }

    @Test
    fun `completed false returns only in progress projects`() {
        mockMvc.get("/projects") {
            param("completed", "false")
        }.andExpect {
            status { isOk() }
            jsonPath("$.content[0].title", `is`(inProgress.title))
        }
    }

    @Test
    fun `professor name filter returns both projects`() {
        mockMvc.get("/projects") {
            param("professor.name", "mart")
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalElements", `is`(2))
        }
    }

    @Test
    fun `student name filter returns only project with that student`() {
        mockMvc.get("/projects") {
            param("student.name", "diaz")
        }.andExpect {
            status { isOk() }
            jsonPath("$.content.size()", `is`(1))
            jsonPath("$.content[0].title", `is`(completed.title))
        }
    }

    @Test
    fun `domain filter returns both`() {
        mockMvc.get("/projects") {
            param("domain", "systems")
        }.andExpect {
            status { isOk() }
            jsonPath("$.totalElements", `is`(2))
        }
    }

    @Test
    fun `invalid completed value yields 400`() {
        mockMvc.get("/projects") {
            param("completed", "maybe")
        }.andExpect {
            status { isBadRequest() }
        }
    }
}
