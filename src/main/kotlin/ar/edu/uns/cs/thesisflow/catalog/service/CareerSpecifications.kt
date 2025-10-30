package ar.edu.uns.cs.thesisflow.catalog.service

import ar.edu.uns.cs.thesisflow.catalog.persistance.entity.Career
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification

data class CareerFilter(
    val name: String? = null,
) {
    companion object { fun empty() = CareerFilter() }
    val isEmpty: Boolean get() = name == null
}

object CareerSpecifications {
    fun withFilter(filter: CareerFilter): Specification<Career> {
        if (filter.isEmpty) return Specification<Career> { _, _, _ -> null }
        return Specification { root, query, cb ->
            query?.distinct(true)
            val predicates = mutableListOf<Predicate>()

            // Name LIKE
            filter.name?.takeIf { it.isNotBlank() }?.let { n ->
                val pattern = "%${n.lowercase()}%"
                predicates += cb.like(cb.lower(root.get<String>("name")), pattern)
            }

            cb.and(*predicates.toTypedArray())
        }
    }
}
