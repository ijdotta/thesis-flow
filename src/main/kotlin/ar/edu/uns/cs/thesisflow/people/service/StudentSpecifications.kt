package ar.edu.uns.cs.thesisflow.people.service

import ar.edu.uns.cs.thesisflow.people.persistance.entity.Student
import jakarta.persistence.criteria.JoinType
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification

data class StudentFilter(
    val lastname: String? = null,
    val name: String? = null,
    val studentId: String? = null,
    val email: String? = null,
) {
    companion object { fun empty() = StudentFilter() }
    val isEmpty: Boolean get() = listOf(lastname, name, studentId, email).all { it == null }
}

object StudentSpecifications {
    fun withFilter(filter: StudentFilter): Specification<Student> {
        if (filter.isEmpty) return Specification<Student> { _, _, _ -> null }
        return Specification { root, query, cb ->
            query?.distinct(true)
            val predicates = mutableListOf<Predicate>()
            val personJoin = root.join<Student, Any>("person", JoinType.LEFT)

            // Lastname LIKE - join to person
            filter.lastname?.takeIf { it.isNotBlank() }?.let { lname ->
                val pattern = "%${lname.lowercase()}%"
                predicates += cb.like(cb.lower(personJoin.get("lastname")), pattern)
            }

            // Name LIKE - join to person
            filter.name?.takeIf { it.isNotBlank() }?.let { n ->
                val pattern = "%${n.lowercase()}%"
                predicates += cb.like(cb.lower(personJoin.get("name")), pattern)
            }

            // StudentId LIKE (prefix/substring match)
            filter.studentId?.takeIf { it.isNotBlank() }?.let { sid ->
                val pattern = "${sid.lowercase()}%"
                predicates += cb.like(cb.lower(root.get("studentId")), pattern)
            }

            // Email LIKE - stored on student
            filter.email?.takeIf { it.isNotBlank() }?.let { e ->
                val pattern = "%${e.lowercase()}%"
                predicates += cb.like(cb.lower(root.get("email")), pattern)
            }

            cb.and(*predicates.toTypedArray())
        }
    }
}
