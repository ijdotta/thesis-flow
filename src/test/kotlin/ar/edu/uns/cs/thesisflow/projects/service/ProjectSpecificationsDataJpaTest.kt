package ar.edu.uns.cs.thesisflow.projects.service

import ar.edu.uns.cs.thesisflow.catalog.persistance.entity.Career
import ar.edu.uns.cs.thesisflow.catalog.persistance.repository.CareerRepository
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.persistance.repository.PersonRepository
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

@DataJpaTest
@Transactional
class ProjectSpecificationsDataJpaTest @Autowired constructor(
    private val projectRepository: ProjectRepository,
    private val participantRepository: ProjectParticipantRepository,
    private val domainRepository: ApplicationDomainRepository,
    private val personRepository: PersonRepository,
    private val careerRepository: CareerRepository,
) {
    private lateinit var projectCompleted: Project
    private lateinit var projectInProgress: Project

    @BeforeEach
    fun setup() {
        val domain = domainRepository.save(ApplicationDomain(name = "Machine Learning"))
        val career = careerRepository.save(Career(name = "Career ${System.nanoTime()}"))
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
        val profPerson = personRepository.save(Person(name = "Alice", lastname = "Garcia"))
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
        assertThat(page.totalElements).isEqualTo(2)
    }
}
