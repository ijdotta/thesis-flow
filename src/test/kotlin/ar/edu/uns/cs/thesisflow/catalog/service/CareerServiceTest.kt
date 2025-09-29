package ar.edu.uns.cs.thesisflow.catalog.service

import ar.edu.uns.cs.thesisflow.catalog.dto.CareerDTO
import ar.edu.uns.cs.thesisflow.catalog.persistance.entity.Career
import ar.edu.uns.cs.thesisflow.catalog.persistance.repository.CareerRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest

@DataJpaTest
class CareerServiceTest @Autowired constructor(
    private val repository: CareerRepository
) {
    private val service = CareerService(repository)

    @Test
    fun `findAll returns page mapped to DTO`() {
        repository.save(Career(name = "Software"))
        val page = service.findAll(PageRequest.of(0,5))
        assertThat(page.content).hasSize(1)
        assertThat(page.content.first().name).isEqualTo("Software")
    }

    @Test
    fun `create validates and saves`() {
        val created = service.create(CareerDTO(name = "Networks"))
        assertThat(created.name).isEqualTo("Networks")
    }

    @Test
    fun `update changes name`() {
        val existing = repository.save(Career(name = "Old"))
        val updated = service.update(CareerDTO(publicId = existing.publicId.toString(), name = "New"))
        assertThat(updated.name).isEqualTo("New")
    }
}
