package ar.edu.uns.cs.thesisflow.projects.service

import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ParticipantRole
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.Project
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectParticipant
import jakarta.persistence.criteria.JoinType
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification
import java.time.Instant

/** Represents nullability filter for certain date fields */
enum class NullabilityFilter { NULL, NOT_NULL }

data class ProjectFilter(
    val title: String? = null,
    val professorName: String? = null,
    val studentName: String? = null,
    val domain: String? = null,
    val completion: NullabilityFilter? = null, // derived from completed=true/false
) {
    companion object { fun empty() = ProjectFilter() }
    val isEmpty: Boolean get() = listOf(title, professorName, studentName, domain, completion).all { it == null }
}

object ProjectSpecifications {
    fun withFilter(filter: ProjectFilter): Specification<Project> {
        // Return a neutral Specification instead of deprecated Specification.where(null)
        if (filter.isEmpty) return Specification<Project> { _, _, _ -> null }
        return Specification { root, query, cb ->
            query?.distinct(true) // safe call for platform type
            val predicates = mutableListOf<Predicate>()

            filter.title?.takeIf { it.isNotBlank() }?.let { t ->
                val pattern = "%${t.lowercase()}%"
                predicates += cb.like(cb.lower(root.get("title")), pattern)
            }

            filter.domain?.takeIf { it.isNotBlank() }?.let { d ->
                val joinDomain = root.join<Project, Any>("applicationDomain", JoinType.LEFT)
                predicates += cb.like(cb.lower(joinDomain.get("name")), "%${d.lowercase()}%")
            }

            filter.completion?.let { nf ->
                predicates += when (nf) {
                    NullabilityFilter.NULL -> cb.isNull(root.get<Instant?>("completion"))
                    NullabilityFilter.NOT_NULL -> cb.isNotNull(root.get<Instant?>("completion"))
                }
            }

            filter.professorName?.takeIf { it.isNotBlank() }?.let { nameFragment ->
                val fragment = "%${nameFragment.lowercase()}%"
                val joinParticipant = root.join<Project, ProjectParticipant>("participants", JoinType.LEFT)
                val joinPerson = joinParticipant.join<ProjectParticipant, Person>("person", JoinType.LEFT)
                val rolePath = joinParticipant.get<ParticipantRole>("participantRole")
                val namePredicate = cb.or(
                    cb.like(cb.lower(joinPerson.get("name")), fragment),
                    cb.like(cb.lower(joinPerson.get("lastname")), fragment)
                )
                predicates += cb.and(rolePath.`in`(ParticipantRole.DIRECTOR, ParticipantRole.CO_DIRECTOR), namePredicate)
            }

            filter.studentName?.takeIf { it.isNotBlank() }?.let { nameFragment ->
                val fragment = "%${nameFragment.lowercase()}%"
                val joinParticipant = root.join<Project, ProjectParticipant>("participants", JoinType.LEFT)
                val joinPerson = joinParticipant.join<ProjectParticipant, Person>("person", JoinType.LEFT)
                val rolePath = joinParticipant.get<ParticipantRole>("participantRole")
                val namePredicate = cb.or(
                    cb.like(cb.lower(joinPerson.get("name")), fragment),
                    cb.like(cb.lower(joinPerson.get("lastname")), fragment)
                )
                predicates += cb.and(rolePath.`in`(ParticipantRole.STUDENT), namePredicate)
            }

            cb.and(*predicates.toTypedArray())
        }
    }
}
