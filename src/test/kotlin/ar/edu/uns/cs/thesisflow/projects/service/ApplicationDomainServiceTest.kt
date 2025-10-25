package ar.edu.uns.cs.thesisflow.projects.service

import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ApplicationDomain
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ApplicationDomainRepository
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ProjectRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest

@DataJpaTest
class ApplicationDomainServiceTest @Autowired constructor(
    private val repository: ApplicationDomainRepository,
    private val projectRepository: ProjectRepository,
) {
    private val service = ApplicationDomainService(repository, projectRepository)

    @Test
    fun `findAll returns page of DTOs`() {
        repository.save(ApplicationDomain(name = "AI"))
        val page = service.findAll(PageRequest.of(0,10))
        assertThat(page.content).hasSize(1)
        assertThat(page.content.first().name).isEqualTo("AI")
    }
}
