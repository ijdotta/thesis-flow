package ar.edu.uns.cs.thesisflow.projects.service

import ar.edu.uns.cs.thesisflow.people.service.PersonService
import ar.edu.uns.cs.thesisflow.projects.dto.ProjectDTO
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.*
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.*
import ar.edu.uns.cs.thesisflow.people.persistance.repository.PersonRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import java.time.LocalDate

@DataJpaTest
@Suppress("unused")
class ProjectServiceTest @Autowired constructor(
    private val projectRepository: ProjectRepository,
    private val applicationDomainRepository: ApplicationDomainRepository,
    private val tagRepository: TagRepository,
    private val projectParticipantRepository: ProjectParticipantRepository,
    private val personRepository: PersonRepository,
) {
    private val personService = PersonService(personRepository)
    private val projectService = ProjectService(
        projectRepository,
        applicationDomainRepository,
        tagRepository,
        projectParticipantRepository,
        personService
    )

    @Test
    fun `findAll with filter returns matching project`() {
        projectRepository.save(Project(title = "Filtered Project", type = ProjectType.THESIS, subType = mutableSetOf(ProjectSubType.TYPE_1)))
        val page = projectService.findAll(PageRequest.of(0,5), ProjectFilter(title = "Filtered"))
        assertThat(page.totalElements).isEqualTo(1)
        assertThat(page.content.first().title).isEqualTo("Filtered Project")
    }

    @Test
    fun `setTags replaces tags`() {
        val project = projectRepository.save(Project(title = "Taggy", type = ProjectType.THESIS, subType = mutableSetOf(ProjectSubType.TYPE_1)))
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
            subtype = listOf(ProjectSubType.TYPE_1.name),
            initialSubmission = LocalDate.now()
        )
        val created = projectService.create(dto)
        assertThat(created.title).isEqualTo("New")
        assertThat(created.subtype).contains(ProjectSubType.TYPE_1.name)
    }
}
