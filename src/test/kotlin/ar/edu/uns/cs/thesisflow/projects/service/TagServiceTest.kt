package ar.edu.uns.cs.thesisflow.projects.service

import ar.edu.uns.cs.thesisflow.projects.persistance.entity.Tag
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.TagRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest

@DataJpaTest
class TagServiceTest @Autowired constructor(
    private val repository: TagRepository
) {
    private val service = TagService(repository)

    @Test
    fun `findAll returns page of DTOs`() {
        repository.save(Tag(name = "Kotlin"))
        val page = service.findAll(PageRequest.of(0,5))
        assertThat(page.content).hasSize(1)
        assertThat(page.content.first().name).isEqualTo("Kotlin")
    }

    @Test
    fun `update applies name and description`() {
        val tag = repository.save(Tag(name = "Java", description = "Old"))
        val updated = service.update(tag.publicId.toString(), ar.edu.uns.cs.thesisflow.projects.dto.TagDTO(publicId = tag.publicId.toString(), name = tag.name, description = "New Desc"))
        assertThat(updated.description).isEqualTo("New Desc")
    }
}
