package ar.edu.uns.cs.thesisflow.projects.service

import ar.edu.uns.cs.thesisflow.catalog.persistance.entity.Career
import ar.edu.uns.cs.thesisflow.catalog.persistance.repository.CareerRepository
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Professor
import ar.edu.uns.cs.thesisflow.people.persistance.repository.PersonRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.ProfessorRepository
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.*
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ProjectParticipantRepository
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ProjectRepository
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ApplicationDomainRepository
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import java.time.LocalDate
import java.util.*

@DataJpaTest
@Transactional
class ProjectSpecificationsDataJpaTest @Autowired constructor(
    private val projectRepository: ProjectRepository,
    private val participantRepository: ProjectParticipantRepository,
    private val domainRepository: ApplicationDomainRepository,
    private val personRepository: PersonRepository,
    private val careerRepository: CareerRepository,
    private val professorRepository: ProfessorRepository,
) {
    private lateinit var projectCompleted: Project
    private lateinit var projectInProgress: Project
    private lateinit var profPerson: Person
    private lateinit var professor: Professor
    private lateinit var differentCareer: Career

    @BeforeEach
    fun setup() {
        val domain = domainRepository.save(ApplicationDomain(name = "Machine Learning"))
        val career = careerRepository.save(Career(name = "Career ${System.nanoTime()}"))
        differentCareer = careerRepository.save(Career(name = "Different Career ${System.nanoTime()}"))
        
        projectCompleted = projectRepository.save(Project(
            title = "Graph Neural Networks",
            type = ProjectType.THESIS,
            subType = mutableSetOf(ProjectSubType.TYPE_1),
            applicationDomain = domain,
            completion = LocalDate.now(),
            career = career
        ))
        projectInProgress = projectRepository.save(Project(
            title = "Vision Transformers",
            type = ProjectType.THESIS,
            subType = mutableSetOf(ProjectSubType.TYPE_1),
            applicationDomain = domain,
            completion = null,
            career = career
        ))
        
        // Create a project with a different career for career filter testing
        projectRepository.save(Project(
            title = "Data Science Project",
            type = ProjectType.FINAL_PROJECT,
            subType = mutableSetOf(ProjectSubType.TYPE_2),
            applicationDomain = domain,
            completion = null,
            career = differentCareer
        ))
        
        profPerson = personRepository.save(Person(name = "Alice", lastname = "Garcia"))
        professor = professorRepository.save(Professor(person = profPerson, email = "alice.garcia+${System.nanoTime()}@example.com"))
        val studPerson = personRepository.save(Person(name = "Bob", lastname = "Lopez"))
        
        participantRepository.save(ProjectParticipant(project = projectCompleted, person = profPerson, participantRole = ParticipantRole.DIRECTOR))
        participantRepository.save(ProjectParticipant(project = projectCompleted, person = studPerson, participantRole = ParticipantRole.STUDENT))
        participantRepository.save(ProjectParticipant(project = projectInProgress, person = profPerson, participantRole = ParticipantRole.CO_DIRECTOR))
    }

    @Test
    fun `filters by completed true`() {
        val filter = ProjectFilter(completion = NullabilityFilter.NOT_NULL)
        val page = projectRepository.findAll(ProjectSpecifications.withFilter(filter), PageRequest.of(0,10))
        val titles = page.content.map { it.title }
        assertThat(titles).contains("Graph Neural Networks").doesNotContain("Vision Transformers")
    }

    @Test
    fun `filters by completed false`() {
        val filter = ProjectFilter(completion = NullabilityFilter.NULL)
        val page = projectRepository.findAll(ProjectSpecifications.withFilter(filter), PageRequest.of(0,10))
        val titles = page.content.map { it.title }
        assertThat(titles).contains("Vision Transformers").doesNotContain("Graph Neural Networks")
    }

    @Test
    fun `filters by professor name fragment`() {
        val filter = ProjectFilter(professorName = "garc")
        val page = projectRepository.findAll(ProjectSpecifications.withFilter(filter), PageRequest.of(0,10))
        assertThat(page.totalElements).isEqualTo(2) // same professor participates in both
    }

    @Test
    fun `filters by student name fragment only returns projects with that student`() {
        val filter = ProjectFilter(studentName = "lopez")
        val page = projectRepository.findAll(ProjectSpecifications.withFilter(filter), PageRequest.of(0,10))
        assertThat(page.content).hasSize(1)
        assertThat(page.content.first().title).isEqualTo("Graph Neural Networks")
    }

    @Test
    fun `filters by domain`() {
        val filter = ProjectFilter(domain = "learning")
        val page = projectRepository.findAll(ProjectSpecifications.withFilter(filter), PageRequest.of(0,10))
        assertThat(page.totalElements).isEqualTo(3)
    }

    @Test
    fun `filters by career name`() {
        val filter = ProjectFilter(career = differentCareer.name)
        val page = projectRepository.findAll(ProjectSpecifications.withFilter(filter), PageRequest.of(0,10))
        assertThat(page.content).hasSize(1)
        assertThat(page.content.first().title).isEqualTo("Data Science Project")
    }

    @Test
    fun `filters by professor public ID returns projects directed or co-directed by that professor`() {
        val filter = ProjectFilter(professorPublicId = professor.publicId.toString())
        val page = projectRepository.findAll(ProjectSpecifications.withFilter(filter), PageRequest.of(0,10))
        assertThat(page.totalElements).isEqualTo(2) // Both projectCompleted and projectInProgress
        val titles = page.content.map { it.title }.toSet()
        assertThat(titles).contains("Graph Neural Networks", "Vision Transformers")
    }

    @Test
    fun `filters by professor public ID returns nothing for non-existent professor`() {
        val filter = ProjectFilter(professorPublicId = UUID.randomUUID().toString())
        val page = projectRepository.findAll(ProjectSpecifications.withFilter(filter), PageRequest.of(0,10))
        assertThat(page.totalElements).isEqualTo(0)
    }

    @Test
    fun `combines career and completion filters`() {
        val filter = ProjectFilter(
            career = differentCareer.name,
            completion = NullabilityFilter.NULL
        )
        val page = projectRepository.findAll(ProjectSpecifications.withFilter(filter), PageRequest.of(0,10))
        assertThat(page.content).hasSize(1)
        assertThat(page.content.first().title).isEqualTo("Data Science Project")
    }

    @Test
    fun `combines professor ID and career filters`() {
        // Professor is only in projects with first career, not differentCareer
        val filter = ProjectFilter(
            professorPublicId = professor.publicId.toString(),
            career = differentCareer.name
        )
        val page = projectRepository.findAll(ProjectSpecifications.withFilter(filter), PageRequest.of(0,10))
        assertThat(page.totalElements).isEqualTo(0) // No overlap
    }
}
