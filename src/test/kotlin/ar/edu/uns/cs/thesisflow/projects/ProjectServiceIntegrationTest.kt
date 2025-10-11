package ar.edu.uns.cs.thesisflow.projects

import ar.edu.uns.cs.thesisflow.people.dto.PersonDTO
import ar.edu.uns.cs.thesisflow.people.service.PersonService
import ar.edu.uns.cs.thesisflow.projects.dto.ApplicationDomainDTO
import ar.edu.uns.cs.thesisflow.projects.dto.ParticipantInfo
import ar.edu.uns.cs.thesisflow.projects.dto.ProjectDTO
import ar.edu.uns.cs.thesisflow.projects.dto.TagDTO
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ParticipantRole
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectSubType
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectType
import ar.edu.uns.cs.thesisflow.projects.service.ApplicationDomainService
import ar.edu.uns.cs.thesisflow.projects.service.ProjectService
import ar.edu.uns.cs.thesisflow.projects.service.TagService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import java.time.LocalDate
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Ignore
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = [
    "spring.datasource.url=jdbc:postgresql://localhost:5432/thesis_flow",
    "spring.datasource.username=thesis_flow_owner",
    "spring.datasource.password=owner",
    "spring.jpa.hibernate.ddl-auto=update"
])
class ProjectServiceIntegrationTest(
    @Autowired val projectService: ProjectService,
    @Autowired val applicationDomainService: ApplicationDomainService,
    @Autowired val tagService: TagService,
    @Autowired val personService: PersonService,
) {
    private lateinit var applicationDomains: List<ApplicationDomainDTO>
    private lateinit var tags: List<TagDTO>
    private lateinit var people: List<PersonDTO>

    @BeforeEach
    fun setup() {
        insertApplicationDomains()
        insertTags()
        insertPeople()
    }

    private fun insertApplicationDomains() {
        applicationDomains = (0..5).map {
            ApplicationDomainDTO(
                name = "domain-$it",
                description = "this is domain $it",
            )
        }.map {
            applicationDomainService.create(it)
        }.toList()
    }

    private fun insertTags() {
        tags = (0..5).map {
            TagDTO(
                name = "tag-$it",
                description = "this is tag $it",
            )
        }.map {
            tagService.create(it)
        }.toList()
    }

    private fun insertPeople() {
        people = (0..5).map {
            PersonDTO(
                name = "name-$it",
                lastname = "lastname-$it",
            )
        }.map {
            personService.create(it)
        }.toList()
    }

    @Test
    fun `create project happy path`() {
        val projectDTO = getProjectDTO()
        val project = projectService.create(projectDTO)

        with(project) {
            assertNotNull(this)
            assertNotNull(publicId)
            assertEquals(projectDTO.type, type)
            assertEquals(projectDTO.subtype, subtype)
        }
    }

    @Test
    fun `add project participants happy path`() {
        val project = projectService.create(getProjectDTO())
        val participantsInfo = listOf(
            ParticipantInfo(people[0].publicId!!, ParticipantRole.DIRECTOR.name),
            ParticipantInfo(people[1].publicId!!, ParticipantRole.CO_DIRECTOR.name),
            ParticipantInfo(people[2].publicId!!, ParticipantRole.COLLABORATOR.name),
            ParticipantInfo(people[3].publicId!!, ParticipantRole.STUDENT.name),
        )

        val projectWithParticipants = projectService.setParticipants(project.publicId!!, participantsInfo)

        val participants = projectWithParticipants.participants
        assertNotNull(participants)
        participantsInfo.forEach { participant ->
            assertTrue { participants.any {
                it.personDTO.publicId == participant.personId &&
                it.role == participant.role
            } }
        }
    }

    @Test
    fun `add project tags happy path`() {
        val project = projectService.create(getProjectDTO())
        val tagIds = tags.map { it.publicId!! }

        val projectWithTags = projectService.setTags(project.publicId!!, tagIds)

        val tags = projectWithTags.tags
        assertNotNull(tags)
        val addedIds = tags.map { it.publicId!! }.toSet()
        assertEquals(tagIds.toSet(), addedIds)
    }

    @Test
    fun `add project application domains happy path`() {
        val project = projectService.create(getProjectDTO())
        val domainId = applicationDomains[0].publicId!!

        val projectWithApplicationDomain = projectService.setApplicationDomain(project.publicId!!, domainId)

        assertEquals(domainId, projectWithApplicationDomain.applicationDomainDTO?.publicId!!)
    }

    private fun getProjectDTO() = ProjectDTO(
        title = "title",
        type = ProjectType.FINAL_PROJECT.name,
        subtype = listOf(ProjectSubType.TYPE_1.name),
        initialSubmission = LocalDate.now(),
    )
}