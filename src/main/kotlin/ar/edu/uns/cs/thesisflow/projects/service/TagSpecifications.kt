package ar.edu.uns.cs.thesisflow.projects.service

import ar.edu.uns.cs.thesisflow.projects.persistance.entity.Tag
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification

data class TagFilter(
    val name: String? = null,
    val description: String? = null,
) {
    companion object { fun empty() = TagFilter() }
    val isEmpty: Boolean get() = listOf(name, description).all { it == null }
}

object TagSpecifications {
    fun withFilter(filter: TagFilter): Specification<Tag> {
        if (filter.isEmpty) return Specification<Tag> { _, _, _ -> null }
        return Specification { root, query, cb ->
            query?.distinct(true)
            val predicates = mutableListOf<Predicate>()

            // Name LIKE
            filter.name?.takeIf { it.isNotBlank() }?.let { n ->
                val pattern = "%${n.lowercase()}%"
                predicates += cb.like(cb.lower(root.get<String>("name")), pattern)
            }

            // Description LIKE
            filter.description?.takeIf { it.isNotBlank() }?.let { d ->
                val pattern = "%${d.lowercase()}%"
                predicates += cb.like(cb.lower(root.get<String>("description")), pattern)
            }

            cb.and(*predicates.toTypedArray())
        }
    }
}
