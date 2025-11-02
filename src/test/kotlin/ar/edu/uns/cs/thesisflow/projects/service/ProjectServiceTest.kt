package ar.edu.uns.cs.thesisflow.projects.service

import ar.edu.uns.cs.thesisflow.auth.model.AuthUser
import ar.edu.uns.cs.thesisflow.auth.model.AuthUserPrincipal
import ar.edu.uns.cs.thesisflow.auth.model.UserRole
import ar.edu.uns.cs.thesisflow.auth.service.CurrentUserService
import ar.edu.uns.cs.thesisflow.catalog.persistance.entity.Career
import ar.edu.uns.cs.thesisflow.catalog.persistance.repository.CareerRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.ProfessorRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.StudentCareerRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.StudentRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.PersonRepository
import ar.edu.uns.cs.thesisflow.projects.dto.ProjectDTO
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.*
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.time.LocalDate

@DataJpaTest
@Suppress("unused")
class ProjectServiceTest @Autowired constructor(
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
    private val mockCsvParser = Mockito.mock(ar.edu.uns.cs.thesisflow.projects.bulk.ProjectCsvParser::class.java)
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

    private lateinit var defaultCareer: Career

    @BeforeEach
    fun setup() {
        defaultCareer = careerRepository.save(Career(name = "Career ${System.nanoTime()}"))
        val authUser = AuthUser(
            username = "admin-test",
            password = "noop",
            role = UserRole.ADMIN,
        )
        val principal = AuthUserPrincipal.from(authUser)
        val authentication = UsernamePasswordAuthenticationToken(
            principal,
            null,
            principal.authorities
        )
        SecurityContextHolder.getContext().authentication = authentication
    }

    @AfterEach
    fun cleanup() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `findAll with filter returns matching project`() {
        val project = Project(
            title = "Filtered Project",
            type = ProjectType.THESIS,
            subType = mutableSetOf(ProjectSubType.INVESTIGACION)
        )
        project.career = defaultCareer
        projectRepository.save(project)
        val page = projectService.findAll(PageRequest.of(0,5), ProjectFilter(title = "Filtered"))
        assertThat(page.totalElements).isEqualTo(1)
        assertThat(page.content.first().title).isEqualTo("Filtered Project")
    }

    @Test
    fun `setTags replaces tags`() {
        val project = Project(title = "Taggy", type = ProjectType.THESIS, subType = mutableSetOf(ProjectSubType.INVESTIGACION))
        project.career = defaultCareer
        projectRepository.save(project)
        val tag = tagRepository.save(Tag(name = "Kotlin"))
        val updated = projectService.setTags(project.publicId.toString(), listOf(tag.publicId.toString()))
        assertThat(updated.tags).isNotNull()
        assertThat(updated.tags!!.first().name).isEqualTo("Kotlin")
    }

    @Test
    fun `create persists project`() {
        val dto = ProjectDTO(
            title = "New",
            type = ProjectType.THESIS.name,
            subtype = listOf(ProjectSubType.INVESTIGACION.name),
            initialSubmission = LocalDate.now(),
            careerPublicId = defaultCareer.publicId.toString()
        )
        val created = projectService.create(dto)
        assertThat(created.title).isEqualTo("New")
        assertThat(created.subtype).contains(ProjectSubType.INVESTIGACION.name)
    }
}
