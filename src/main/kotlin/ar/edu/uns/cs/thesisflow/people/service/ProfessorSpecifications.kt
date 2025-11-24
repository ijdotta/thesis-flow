package ar.edu.uns.cs.thesisflow.people.service

import ar.edu.uns.cs.thesisflow.people.persistance.entity.Professor
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification

data class ProfessorFilter(
    val lastname: String? = null,
    val name: String? = null,
    val email: String? = null,
) {
    companion object { fun empty() = ProfessorFilter() }
    val isEmpty: Boolean get() = listOf(lastname, name, email).all { it == null }
}

object ProfessorSpecifications {
    fun withFilter(filter: ProfessorFilter): Specification<Professor> {
        if (filter.isEmpty) return Specification<Professor> { _, _, _ -> null }
        return Specification { root, _, cb ->
            val predicates = mutableListOf<Predicate>()

            // Lastname LIKE - join to person
            filter.lastname?.takeIf { it.isNotBlank() }?.let { lname ->
                val pattern = "%${lname.lowercase()}%"
                val personJoin = root.join<Professor, Any>("person")
                predicates += cb.like(cb.lower(personJoin.get("lastname")), pattern)
            }

            // Name LIKE - join to person
            filter.name?.takeIf { it.isNotBlank() }?.let { n ->
                val pattern = "%${n.lowercase()}%"
                val personJoin = root.join<Professor, Any>("person")
                predicates += cb.like(cb.lower(personJoin.get("name")), pattern)
            }

            // Email LIKE
            filter.email?.takeIf { it.isNotBlank() }?.let { e ->
                val pattern = "%${e.lowercase()}%"
                predicates += cb.like(cb.lower(root.get("email")), pattern)
            }

            cb.and(*predicates.toTypedArray())
        }
    }
}

