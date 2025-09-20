package ar.edu.uns.cs.thesisflow.projects

import ar.edu.uns.cs.thesisflow.projects.dto.ApplicationDomainDTO
import ar.edu.uns.cs.thesisflow.projects.dto.ProjectDTO
import ar.edu.uns.cs.thesisflow.projects.dto.TagDTO
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectSubType
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectType
import ar.edu.uns.cs.thesisflow.projects.service.ApplicationDomainService
import ar.edu.uns.cs.thesisflow.projects.service.ProjectService
import ar.edu.uns.cs.thesisflow.projects.service.TagService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = [
    "spring.datasource.url=jdbc:postgresql://localhost:5432/thesis_flow",
    "spring.datasource.username=thesis_flow_owner",
    "spring.datasource.password=owner",
    "spring.jpa.hibernate.ddl-auto=create-drop"
])
class ProjectServiceIntegrationTest(
    @Autowired val projectService: ProjectService,
    @Autowired val applicationDomainService: ApplicationDomainService,
    @Autowired val tagService: TagService,
) {
    private lateinit var applicationDomains: List<ApplicationDomainDTO>
    private lateinit var tags: List<TagDTO>

    @BeforeEach
    fun setup() {
        insertApplicationDomains()
        insertTags()
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

    @Test
    fun `create project happy path`() {
        val projectDTO = ProjectDTO(
            title = "title",
            type = ProjectType.FINAL_PROJECT.name,
            subtype = listOf(ProjectSubType.TYPE_1.name),
            initialSubmission = Instant.now(),
        )
        val project = projectService.create(projectDTO)

        with(project) {
            assertNotNull(this)
            assertNotNull(publicId)
            assertEquals(projectDTO.type, type)
            assertEquals(projectDTO.subtype, subtype)
        }
    }
}