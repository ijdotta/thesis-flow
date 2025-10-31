package ar.edu.uns.cs.thesisflow.people.service

import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification

data class PersonFilter(
    val lastname: String? = null,
    val name: String? = null,
) {
    companion object { fun empty() = PersonFilter() }
    val isEmpty: Boolean get() = lastname == null && name == null
}

object PersonSpecifications {
    fun withFilter(filter: PersonFilter): Specification<Person> {
        if (filter.isEmpty) return Specification<Person> { _, _, _ -> null }
        return Specification { root, _, cb ->
            val predicates = mutableListOf<Predicate>()

            filter.lastname?.takeIf { it.isNotBlank() }?.let { lname ->
                val pattern = "%${lname.lowercase()}%"
                predicates += cb.like(cb.lower(root.get("lastname")), pattern)
            }

            filter.name?.takeIf { it.isNotBlank() }?.let { n ->
                val pattern = "%${n.lowercase()}%"
                predicates += cb.like(cb.lower(root.get("name")), pattern)
            }

            cb.and(*predicates.toTypedArray())
        }
    }
}
