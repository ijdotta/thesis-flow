package ar.edu.uns.cs.thesisflow.projects.service

import ar.edu.uns.cs.thesisflow.auth.model.AuthUser
import ar.edu.uns.cs.thesisflow.auth.model.AuthUserPrincipal
import ar.edu.uns.cs.thesisflow.auth.model.UserRole
import ar.edu.uns.cs.thesisflow.auth.service.CurrentUserService
import ar.edu.uns.cs.thesisflow.catalog.persistance.entity.Career
import ar.edu.uns.cs.thesisflow.catalog.persistance.repository.CareerRepository
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Professor
import ar.edu.uns.cs.thesisflow.people.persistance.repository.ProfessorRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.PersonRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.StudentCareerRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.StudentRepository
import ar.edu.uns.cs.thesisflow.projects.api.ProjectResourceRequest
import ar.edu.uns.cs.thesisflow.projects.bulk.ProjectCsvParser
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.*
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.time.LocalDate

@DataJpaTest
@Suppress("unused")
class ProjectResourcesTest @Autowired constructor(
    private val projectRepository: ProjectRepository,
    private val applicationDomainRepository: ApplicationDomainRepository,
    private val tagRepository: TagRepository,
    private val projectParticipantRepository: ProjectParticipantRepository,
    private val studentRepository: StudentRepository,
    private val professorRepository: ProfessorRepository,
    private val personRepository: PersonRepository,
    private val careerRepository: CareerRepository,
    private val studentCareerRepository: StudentCareerRepository,
) {
    private val currentUserService = CurrentUserService()
    private val projectAuthorizationService = ProjectAuthorizationService(
        currentUserService,
        professorRepository,
        projectParticipantRepository
    )
    private val mockCsvParser = Mockito.mock(ProjectCsvParser::class.java)
    private val projectService = ProjectService(
        projectRepository,
        applicationDomainRepository,
        tagRepository,
        projectParticipantRepository,
        studentRepository,
        professorRepository,
        personRepository,
        careerRepository,
        studentCareerRepository,
        projectAuthorizationService,
        mockCsvParser
    )

    private lateinit var career: Career
    private lateinit var project: Project
    private lateinit var professorPerson: Person
    private lateinit var professor: Professor

    @BeforeEach
    fun setup() {
        // Create career
        career = Career(name = "Computer Science")
        careerRepository.save(career)

        // Create professor
        professorPerson = Person(name = "Dr.", lastname = "Smith")
        personRepository.save(professorPerson)
        professor = Professor(person = professorPerson, email = "smith@example.com")
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
            person = professorPerson,
            participantRole = ParticipantRole.DIRECTOR
        )
        projectParticipantRepository.save(participant)

        // Setup security context for professor
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
    fun `updateResources adds resources to project`() {
        val requests = listOf(
            ProjectResourceRequest(
                url = "https://github.com/repo",
                title = "Repository",
                description = "Main repo"
            ),
            ProjectResourceRequest(
                url = "https://drive.google.com/file/d/123",
                title = "Data",
                description = null
            )
        )

        val result = projectService.updateResources(project.publicId.toString(), requests)

        assertThat(result.resources).hasSize(2)
        assertThat(result.resources!![0].url).isEqualTo("https://github.com/repo")
        assertThat(result.resources!![0].title).isEqualTo("Repository")
        assertThat(result.resources!![0].description).isEqualTo("Main repo")
        assertThat(result.resources!![1].url).isEqualTo("https://drive.google.com/file/d/123")
        assertThat(result.resources!![1].title).isEqualTo("Data")
    }

    @Test
    fun `updateResources replaces entire resource list`() {
        // First update
        val firstRequests = listOf(
            ProjectResourceRequest(
                url = "https://github.com/old",
                title = "Old",
                description = "Old resource"
            )
        )
        projectService.updateResources(project.publicId.toString(), firstRequests)

        // Second update
        val secondRequests = listOf(
            ProjectResourceRequest(
                url = "https://github.com/new",
                title = "New",
                description = "New resource"
            )
        )
        val result = projectService.updateResources(project.publicId.toString(), secondRequests)

        assertThat(result.resources).hasSize(1)
        assertThat(result.resources!![0].url).isEqualTo("https://github.com/new")
    }

    @Test
    fun `updateResources clears resources with empty list`() {
        // Add resources first
        val requests = listOf(
            ProjectResourceRequest(
                url = "https://github.com/repo",
                title = "Repository"
            )
        )
        projectService.updateResources(project.publicId.toString(), requests)

        // Clear with empty list
        val result = projectService.updateResources(project.publicId.toString(), emptyList())

        assertThat(result.resources).hasSize(0)
    }

    @Test
    fun `updateResources validates URL format`() {
        val invalidRequests = listOf(
            ProjectResourceRequest(
                url = "not-a-url",
                title = "Invalid"
            )
        )

        assertThatThrownBy {
            projectService.updateResources(project.publicId.toString(), invalidRequests)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("URL must be a valid HTTP or HTTPS URL")
    }

    @Test
    fun `updateResources validates title is not empty`() {
        val invalidRequests = listOf(
            ProjectResourceRequest(
                url = "https://github.com/repo",
                title = ""
            )
        )

        assertThatThrownBy {
            projectService.updateResources(project.publicId.toString(), invalidRequests)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Title cannot be empty")
    }

    @Test
    fun `updateResources validates title max length`() {
        val invalidRequests = listOf(
            ProjectResourceRequest(
                url = "https://github.com/repo",
                title = "a".repeat(256)
            )
        )

        assertThatThrownBy {
            projectService.updateResources(project.publicId.toString(), invalidRequests)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Title cannot exceed 255 characters")
    }

    @Test
    fun `updateResources validates description max length`() {
        val invalidRequests = listOf(
            ProjectResourceRequest(
                url = "https://github.com/repo",
                title = "Valid",
                description = "a".repeat(1001)
            )
        )

        assertThatThrownBy {
            projectService.updateResources(project.publicId.toString(), invalidRequests)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Description cannot exceed 1000 characters")
    }

    @Test
    fun `updateResources requires authorization`() {
        // Switch to different professor without access
        val otherPerson = Person(name = "Dr.", lastname = "Jones")
        personRepository.save(otherPerson)
        val otherProfessor = Professor(person = otherPerson, email = "jones@example.com")
        professorRepository.save(otherProfessor)

        val authUser = AuthUser(
            username = "jones",
            password = "noop",
            role = UserRole.PROFESSOR,
            professor = otherProfessor
        )
        val principal = AuthUserPrincipal.from(authUser)
        val authentication = UsernamePasswordAuthenticationToken(
            principal,
            null,
            principal.authorities
        )
        SecurityContextHolder.getContext().authentication = authentication

        val requests = listOf(
            ProjectResourceRequest(
                url = "https://github.com/repo",
                title = "Repository"
            )
        )

        assertThatThrownBy {
            projectService.updateResources(project.publicId.toString(), requests)
        }.isInstanceOf(Exception::class.java)
    }

    @Test
    fun `updateResources allows ADMIN access`() {
        val adminUser = AuthUser(
            username = "admin",
            password = "noop",
            role = UserRole.ADMIN
        )
        val principal = AuthUserPrincipal.from(adminUser)
        val authentication = UsernamePasswordAuthenticationToken(
            principal,
            null,
            principal.authorities
        )
        SecurityContextHolder.getContext().authentication = authentication

        val requests = listOf(
            ProjectResourceRequest(
                url = "https://github.com/repo",
                title = "Repository"
            )
        )

        val result = projectService.updateResources(project.publicId.toString(), requests)

        assertThat(result.resources).hasSize(1)
    }

    @Test
    fun `updateResources persists to database`() {
        val requests = listOf(
            ProjectResourceRequest(
                url = "https://github.com/repo",
                title = "Repository",
                description = "Test repo"
            )
        )

        projectService.updateResources(project.publicId.toString(), requests)

        // Fetch from database
        val savedProject = projectRepository.findByPublicId(project.publicId)
        assertThat(savedProject).isNotNull

        val objectMapper = ObjectMapper()
        val resources = objectMapper.readValue(
            savedProject!!.resources,
            Array<com.fasterxml.jackson.databind.JsonNode>::class.java
        )
        assertThat(resources).hasSize(1)
        assertThat(resources[0]["url"].asText()).isEqualTo("https://github.com/repo")
    }

    @Test
    fun `updateResources accepts http URLs`() {
        val requests = listOf(
            ProjectResourceRequest(
                url = "http://example.com/file",
                title = "File"
            )
        )

        val result = projectService.updateResources(project.publicId.toString(), requests)

        assertThat(result.resources).hasSize(1)
        assertThat(result.resources!![0].url).isEqualTo("http://example.com/file")
    }

    @Test
    fun `updateResources accepts https URLs`() {
        val requests = listOf(
            ProjectResourceRequest(
                url = "https://secure.example.com/file",
                title = "File"
            )
        )

        val result = projectService.updateResources(project.publicId.toString(), requests)

        assertThat(result.resources).hasSize(1)
        assertThat(result.resources!![0].url).isEqualTo("https://secure.example.com/file")
    }

    @Test
    fun `updateResources rejects ftp URLs`() {
        val invalidRequests = listOf(
            ProjectResourceRequest(
                url = "ftp://example.com/file",
                title = "File"
            )
        )

        assertThatThrownBy {
            projectService.updateResources(project.publicId.toString(), invalidRequests)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("URL must be a valid HTTP or HTTPS URL")
    }

    @Test
    fun `updateResources allows optional description`() {
        val requests = listOf(
            ProjectResourceRequest(
                url = "https://github.com/repo",
                title = "Repository",
                description = null
            )
        )

        val result = projectService.updateResources(project.publicId.toString(), requests)

        assertThat(result.resources).hasSize(1)
        assertThat(result.resources!![0].description).isNull()
    }

    @Test
    fun `updateResources handles multiple resources validation`() {
        val requests = listOf(
            ProjectResourceRequest(
                url = "https://github.com/repo",
                title = "Valid"
            ),
            ProjectResourceRequest(
                url = "invalid-url",
                title = "Invalid"
            )
        )

        assertThatThrownBy {
            projectService.updateResources(project.publicId.toString(), requests)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `updateResources updates project timestamp`() {
        val beforeUpdate = project.updatedAt

        Thread.sleep(10) // Small delay to ensure timestamp difference

        val requests = listOf(
            ProjectResourceRequest(
                url = "https://github.com/repo",
                title = "Repository"
            )
        )
        projectService.updateResources(project.publicId.toString(), requests)

        val updated = projectRepository.findByPublicId(project.publicId)
        assertThat(updated!!.updatedAt).isAfter(beforeUpdate)
    }
}
