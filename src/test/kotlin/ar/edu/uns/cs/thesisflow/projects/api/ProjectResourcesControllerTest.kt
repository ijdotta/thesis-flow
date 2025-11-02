package ar.edu.uns.cs.thesisflow.projects.api

import ar.edu.uns.cs.thesisflow.auth.model.AuthUser
import ar.edu.uns.cs.thesisflow.auth.model.AuthUserPrincipal
import ar.edu.uns.cs.thesisflow.auth.model.UserRole
import ar.edu.uns.cs.thesisflow.catalog.persistance.entity.Career
import ar.edu.uns.cs.thesisflow.catalog.persistance.repository.CareerRepository
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Professor
import ar.edu.uns.cs.thesisflow.people.persistance.repository.ProfessorRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.PersonRepository
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.*
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ProjectParticipantRepository
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ProjectRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = [
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
    "spring.jpa.hibernate.ddl-auto=create-drop"
])
@Transactional
class ProjectResourcesControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val projectRepository: ProjectRepository,
    @Autowired private val careerRepository: CareerRepository,
    @Autowired private val personRepository: PersonRepository,
    @Autowired private val professorRepository: ProfessorRepository,
    @Autowired private val projectParticipantRepository: ProjectParticipantRepository,
) {
    private lateinit var career: Career
    private lateinit var project: Project
    private lateinit var professor: Professor

    @BeforeEach
    fun setup() {
        // Create career
        career = Career(name = "Computer Science")
        careerRepository.save(career)

        // Create professor
        val person = Person(name = "Dr.", lastname = "Smith")
        personRepository.save(person)
        professor = Professor(person = person, email = "smith@example.com")
        professorRepository.save(professor)

        // Create project
        project = Project(
            title = "Test Project",
            type = ProjectType.THESIS,
            subType = mutableSetOf(ProjectSubType.INVESTIGACION),
            initialSubmission = LocalDate.now(),
            career = career
        )
        projectRepository.save(project)

        // Add professor as director
        val participant = ProjectParticipant(
            project = project,
            person = person,
            participantRole = ParticipantRole.DIRECTOR
        )
        projectParticipantRepository.save(participant)

        // Setup security context
        val authUser = AuthUser(
            username = "smith",
            password = "noop",
            role = UserRole.PROFESSOR,
            professor = professor
        )
        val principal = AuthUserPrincipal.from(authUser)
        val authentication = UsernamePasswordAuthenticationToken(
            principal,
            null,
            principal.authorities
        )
        SecurityContextHolder.getContext().authentication = authentication
    }

    @Test
    fun `PUT resources returns 200 OK with updated project`() {
        val requests = listOf(
            ProjectResourceRequest(
                url = "https://github.com/repo",
                title = "Repository",
                description = "Main repo"
            )
        )

        mockMvc.perform(
            put("/projects/${project.publicId}/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.publicId").exists())
            .andExpect(jsonPath("$.resources").isArray)
            .andExpect(jsonPath("$.resources[0].url").value("https://github.com/repo"))
            .andExpect(jsonPath("$.resources[0].title").value("Repository"))
    }

    @Test
    fun `PUT resources with invalid URL returns 400 Bad Request`() {
        val requests = listOf(
            ProjectResourceRequest(
                url = "not-a-url",
                title = "Invalid"
            )
        )

        mockMvc.perform(
            put("/projects/${project.publicId}/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").exists())
    }

    @Test
    fun `PUT resources with empty title returns 400 Bad Request`() {
        val requests = listOf(
            ProjectResourceRequest(
                url = "https://github.com/repo",
                title = ""
            )
        )

        mockMvc.perform(
            put("/projects/${project.publicId}/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").exists())
    }

    @Test
    fun `PUT resources with title exceeding 255 chars returns 400 Bad Request`() {
        val requests = listOf(
            ProjectResourceRequest(
                url = "https://github.com/repo",
                title = "a".repeat(256)
            )
        )

        mockMvc.perform(
            put("/projects/${project.publicId}/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").exists())
    }

    @Test
    fun `PUT resources with description exceeding 1000 chars returns 400 Bad Request`() {
        val requests = listOf(
            ProjectResourceRequest(
                url = "https://github.com/repo",
                title = "Valid",
                description = "a".repeat(1001)
            )
        )

        mockMvc.perform(
            put("/projects/${project.publicId}/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").exists())
    }

    @Test
    fun `PUT resources with empty array clears all resources`() {
        // First add resources
        val initialRequests = listOf(
            ProjectResourceRequest(
                url = "https://github.com/repo",
                title = "Repository"
            )
        )

        mockMvc.perform(
            put("/projects/${project.publicId}/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(initialRequests))
        )
            .andExpect(status().isOk)

        // Then clear
        mockMvc.perform(
            put("/projects/${project.publicId}/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyList<ProjectResourceRequest>()))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resources").isArray)
            .andExpect(jsonPath("$.resources.length()").value(0))
    }

    @Test
    fun `PUT resources replaces entire list`() {
        // First add resource
        val firstRequests = listOf(
            ProjectResourceRequest(
                url = "https://github.com/old",
                title = "Old"
            )
        )

        mockMvc.perform(
            put("/projects/${project.publicId}/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequests))
        )
            .andExpect(status().isOk)

        // Then replace with new resource
        val secondRequests = listOf(
            ProjectResourceRequest(
                url = "https://github.com/new",
                title = "New"
            )
        )

        mockMvc.perform(
            put("/projects/${project.publicId}/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondRequests))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resources.length()").value(1))
            .andExpect(jsonPath("$.resources[0].url").value("https://github.com/new"))
    }

    @Test
    fun `PUT resources allows optional description`() {
        val requests = listOf(
            ProjectResourceRequest(
                url = "https://github.com/repo",
                title = "Repository",
                description = null
            )
        )

        mockMvc.perform(
            put("/projects/${project.publicId}/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resources[0].description").value(""))
    }

    @Test
    fun `PUT resources accepts http URLs`() {
        val requests = listOf(
            ProjectResourceRequest(
                url = "http://example.com/file",
                title = "File"
            )
        )

        mockMvc.perform(
            put("/projects/${project.publicId}/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resources[0].url").value("http://example.com/file"))
    }

    @Test
    fun `PUT resources with multiple resources succeeds`() {
        val requests = listOf(
            ProjectResourceRequest(
                url = "https://github.com/repo1",
                title = "Repo 1",
                description = "First repo"
            ),
            ProjectResourceRequest(
                url = "https://github.com/repo2",
                title = "Repo 2",
                description = "Second repo"
            ),
            ProjectResourceRequest(
                url = "https://drive.google.com/file/d/123",
                title = "Data",
                description = null
            )
        )

        mockMvc.perform(
            put("/projects/${project.publicId}/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.resources.length()").value(3))
            .andExpect(jsonPath("$.resources[0].title").value("Repo 1"))
            .andExpect(jsonPath("$.resources[1].title").value("Repo 2"))
            .andExpect(jsonPath("$.resources[2].title").value("Data"))
    }

    @Test
    fun `PUT resources with non-existent project returns 500`() {
        val requests = listOf(
            ProjectResourceRequest(
                url = "https://github.com/repo",
                title = "Repository"
            )
        )

        mockMvc.perform(
            put("/projects/00000000-0000-0000-0000-000000000000/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests))
        )
            .andExpect(status().isInternalServerError)
    }

    @Test
    fun `PUT resources without auth returns 401`() {
        SecurityContextHolder.clearContext()

        val requests = listOf(
            ProjectResourceRequest(
                url = "https://github.com/repo",
                title = "Repository"
            )
        )

        mockMvc.perform(
            put("/projects/${project.publicId}/resources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests))
        )
            .andExpect(status().isUnauthorized)
    }
}
