package ar.edu.uns.cs.thesisflow.people.service

import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.persistance.repository.PersonRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import jakarta.transaction.Transactional

@DataJpaTest
@Transactional
class PersonSpecificationsDataJpaTest @Autowired constructor(
    private val personRepository: PersonRepository,
) {
    private lateinit var alice: Person
    private lateinit var bob: Person

    @BeforeEach
    fun setup() {
        alice = personRepository.save(Person(name = "Alice", lastname = "Johnson"))
        bob = personRepository.save(Person(name = "Bob", lastname = "Lopez"))
    }

    @Test
    fun `filters by lastname substring`() {
        val filter = PersonFilter(lastname = "john")
        val page = personRepository.findAll(PersonSpecifications.withFilter(filter), PageRequest.of(0, 10))
        assertThat(page.content.map { it.publicId }).containsExactly(alice.publicId)
    }

    @Test
    fun `filters by name substring`() {
        val filter = PersonFilter(name = "ob")
        val page = personRepository.findAll(PersonSpecifications.withFilter(filter), PageRequest.of(0, 10))
        assertThat(page.content.map { it.publicId }).containsExactly(bob.publicId)
    }

    @Test
    fun `combines name and lastname filters with AND semantics`() {
        val filter = PersonFilter(name = "al", lastname = "john")
        val page = personRepository.findAll(PersonSpecifications.withFilter(filter), PageRequest.of(0, 10))
        assertThat(page.content).hasSize(1)
        assertThat(page.content.first().publicId).isEqualTo(alice.publicId)
    }
}
