package ar.edu.uns.cs.thesisflow.analytics.command

import ar.edu.uns.cs.thesisflow.analytics.dto.NetworkEdge
import ar.edu.uns.cs.thesisflow.analytics.dto.NetworkNode
import ar.edu.uns.cs.thesisflow.analytics.dto.ProfessorNetworkResponse
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ParticipantRole
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectType
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ProjectRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
class GetProfessorNetworkCommand(
    private val projectRepository: ProjectRepository,
) {
    fun execute(
        careerIds: List<UUID>? = null,
        professorIds: List<UUID>? = null,
        projectTypes: List<ProjectType>? = null,
        fromYear: Int? = null,
        toYear: Int? = null,
    ): ProfessorNetworkResponse {
        val projects = projectRepository.findAll()
            .filter { project ->
                careerIds == null || project.career?.publicId in careerIds
            }
            .filter { project ->
                projectTypes == null || project.type in projectTypes
            }
            .filter { project ->
                val projectYear = project.initialSubmission.year
                (fromYear == null || projectYear >= fromYear) &&
                (toYear == null || projectYear <= toYear)
            }

        val professors = mutableMapOf<UUID, Pair<String, Int>>()
        val collaborations = mutableMapOf<Pair<UUID, UUID>, Int>()

        projects.forEach { project ->
            val projectProfessors = project.participants
                .filter { it.participantRole in setOf(ParticipantRole.DIRECTOR, ParticipantRole.CO_DIRECTOR, ParticipantRole.COLLABORATOR) }
                .filter { professorIds == null || it.person.publicId in professorIds }
                .map { it.person.publicId to "${it.person.name} ${it.person.lastname}" }
            
            projectProfessors.forEach { (profId, profName) ->
                professors[profId] = profName to (professors[profId]?.second ?: 0) + 1
            }

            for (i in projectProfessors.indices) {
                for (j in i + 1 until projectProfessors.size) {
                    val prof1 = projectProfessors[i].first
                    val prof2 = projectProfessors[j].first
                    val key = if (prof1 < prof2) prof1 to prof2 else prof2 to prof1
                    collaborations[key] = (collaborations[key] ?: 0) + 1
                }
            }
        }

        val nodes = professors.map { (profId, pair) ->
            val (name, count) = pair
            NetworkNode(
                id = profId.toString(),
                name = name,
                projectCount = count
            )
        }

        val edges = collaborations.map { (key, weight) ->
            val (prof1, prof2) = key
            NetworkEdge(
                source = prof1.toString(),
                target = prof2.toString(),
                weight = weight,
                collaborations = weight
            )
        }

        return ProfessorNetworkResponse(nodes, edges)
    }
}
