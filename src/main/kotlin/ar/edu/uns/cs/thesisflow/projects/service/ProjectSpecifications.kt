package ar.edu.uns.cs.thesisflow.projects.service

import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ParticipantRole
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.Project
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectParticipant
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectType
import jakarta.persistence.criteria.JoinType
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Path
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
    val type: String? = null, // project type enum name
) {
    companion object { fun empty() = ProjectFilter() }
    val isEmpty: Boolean get() = listOf(title, professorName, studentName, domain, completion, type).all { it == null }
}

object ProjectSpecifications {
    fun withFilter(filter: ProjectFilter): Specification<Project> {
        // Return a neutral Specification instead of deprecated Specification.where(null)
        if (filter.isEmpty) return Specification<Project> { _, _, _ -> null }
        return Specification { root, query, cb ->
            query?.distinct(true) // safe call for platform type
            val predicates = mutableListOf<Predicate>()

            // Title LIKE
            filter.title?.takeIf { it.isNotBlank() }?.let { t ->
                val pattern = "%${t.lowercase()}%"
                predicates += cb.like(cb.lower(root.get("title")), pattern)
            }

            // Type equality or IN (comma separated list supported)
            filter.type?.takeIf { it.isNotBlank() }?.let { raw ->
                val typeNames = raw.split(',').map { it.trim() }.filter { it.isNotEmpty() }
                val enums = typeNames.mapNotNull { runCatching { ProjectType.valueOf(it) }.getOrNull() }.distinct()
                when {
                    enums.size == 1 -> predicates += cb.equal(root.get<ProjectType>("type"), enums.first())
                    enums.isNotEmpty() -> predicates += root.get<ProjectType>("type").`in`(enums)
                }
            }

            // Domain name LIKE
            filter.domain?.takeIf { it.isNotBlank() }?.let { d ->
                val joinDomain = root.join<Project, Any>("applicationDomain", JoinType.LEFT)
                predicates += cb.like(cb.lower(joinDomain.get("name")), "%${d.lowercase()}%")
            }

            // Completion nullability
            filter.completion?.let { nf ->
                predicates += when (nf) {
                    NullabilityFilter.NULL -> cb.isNull(root.get<Instant?>("completion"))
                    NullabilityFilter.NOT_NULL -> cb.isNotNull(root.get<Instant?>("completion"))
                }
            }

            // Helper to build OR of name/lastname LIKE for multiple fragments
            fun personNamePredicate(fragments: List<String>, personPath: Path<Person>): Predicate {
                val likePreds = fragments.map { frag ->
                    val pat = "%${frag.lowercase()}%"
                    cb.or(
                        cb.like(cb.lower(personPath.get<String>("name")), pat),
                        cb.like(cb.lower(personPath.get<String>("lastname")), pat)
                    )
                }
                return cb.or(*likePreds.toTypedArray())
            }

            // Professor (director / co-director) name fragment(s) via EXISTS subquery
            filter.professorName?.takeIf { it.isNotBlank() }?.let { rawFragments ->
                val fragments = rawFragments.split(',', ' ').map { it.trim() }.filter { it.isNotEmpty() }
                if (fragments.isNotEmpty()) {
                    val sub = query?.subquery(Long::class.java)
                    if (sub != null) {
                        val pp = sub.from(ProjectParticipant::class.java)
                        val person = pp.join<ProjectParticipant, Person>("person", JoinType.LEFT)
                        val namePred = personNamePredicate(fragments, person)
                        sub.select(cb.literal(1L)).where(
                            cb.equal(pp.get<Project>("project"), root),
                            pp.get<ParticipantRole>("participantRole").`in`(ParticipantRole.DIRECTOR, ParticipantRole.CO_DIRECTOR),
                            namePred
                        )
                        predicates += cb.exists(sub)
                    }
                }
            }

            // Student name fragment(s) via EXISTS subquery
            filter.studentName?.takeIf { it.isNotBlank() }?.let { rawFragments ->
                val fragments = rawFragments.split(',', ' ').map { it.trim() }.filter { it.isNotEmpty() }
                if (fragments.isNotEmpty()) {
                    val sub = query?.subquery(Long::class.java)
                    if (sub != null) {
                        val pp = sub.from(ProjectParticipant::class.java)
                        val person = pp.join<ProjectParticipant, Person>("person", JoinType.LEFT)
                        val namePred = personNamePredicate(fragments, person)
                        sub.select(cb.literal(1L)).where(
                            cb.equal(pp.get<Project>("project"), root),
                            pp.get<ParticipantRole>("participantRole").`in`(ParticipantRole.STUDENT),
                            namePred
                        )
                        predicates += cb.exists(sub)
                    }
                }
            }

            cb.and(*predicates.toTypedArray())
        }
    }
}
