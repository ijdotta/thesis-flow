package ar.edu.uns.cs.thesisflow.people.service

import ar.edu.uns.cs.thesisflow.people.dto.ProfessorDTO
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.persistance.repository.PersonRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.ProfessorRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest

@DataJpaTest
class ProfessorServiceTest @Autowired constructor(
    private val professorRepository: ProfessorRepository,
    private val personRepository: PersonRepository,
) {
    private val service = ProfessorService(professorRepository, personRepository)

    @Test
    fun `findAll returns page mapped to DTO`() {
        val person = personRepository.save(Person(name = "John", lastname = "Doe"))
        professorRepository.save(ar.edu.uns.cs.thesisflow.people.persistance.entity.Professor(person = person, email = "john@cs.uns.edu.ar"))
        val page = service.findAll(PageRequest.of(0,5))
        assertThat(page.content).hasSize(1)
        assertThat(page.content.first().name).isEqualTo("John")
    }

    @Test
    fun `create validates email domain`() {
        val person = personRepository.save(Person(name = "Jane", lastname = "Doe"))
        val dto = ProfessorDTO(personPublicId = person.publicId.toString(), email = "jane@cs.uns.edu.ar")
        val created = service.create(dto)
        assertThat(created.email).isEqualTo("jane@cs.uns.edu.ar")
    }

    @Test
    fun `create rejects invalid email domain`() {
        val person = personRepository.save(Person(name = "Bad", lastname = "Email"))
        val dto = ProfessorDTO(personPublicId = person.publicId.toString(), email = "bad@example.com")
        assertThatThrownBy { service.create(dto) }.isInstanceOf(IllegalArgumentException::class.java)
    }
}
