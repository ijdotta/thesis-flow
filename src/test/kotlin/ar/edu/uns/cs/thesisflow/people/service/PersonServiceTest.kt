package ar.edu.uns.cs.thesisflow.people.service

import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.persistance.repository.PersonRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest

@DataJpaTest
class PersonServiceTest @Autowired constructor(
    private val personRepository: PersonRepository
) {
    private val service = PersonService(personRepository)

    @Test
    fun `findAll maps entities to DTOs`() {
        personRepository.save(Person(name = "Alice", lastname = "Smith"))
        val page = service.findAll(PageRequest.of(0, 10))
        assertThat(page.totalElements).isEqualTo(1)
        assertThat(page.content.first().name).isEqualTo("Alice")
    }

    @Test
    fun `update updates mutable fields`() {
        val saved = personRepository.save(Person(name = "Bob", lastname = "Brown"))
        val dto = ar.edu.uns.cs.thesisflow.people.dto.PersonDTO(publicId = saved.publicId.toString(), name = "Robert", lastname = "Brown")
        val updated = service.update(dto)
        assertThat(updated.name).isEqualTo("Robert")
    }
}
