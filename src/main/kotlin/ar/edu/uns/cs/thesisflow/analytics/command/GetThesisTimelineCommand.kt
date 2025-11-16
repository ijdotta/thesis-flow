package ar.edu.uns.cs.thesisflow.analytics.command

import ar.edu.uns.cs.thesisflow.analytics.dto.ThesisTimelineData
import ar.edu.uns.cs.thesisflow.analytics.dto.ThesisTimelineResponse
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ParticipantRole
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectType
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ProjectRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
class GetThesisTimelineCommand(
    private val projectRepository: ProjectRepository,
) {
    fun execute(
        careerIds: List<UUID>? = null,
        professorIds: List<UUID>? = null,
        projectTypes: List<ProjectType>? = null,
        fromYear: Int? = null,
        toYear: Int? = null,
    ): ThesisTimelineResponse {
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

        val data = projects
            .flatMap { project ->
                project.participants
                    .filter { it.participantRole in setOf(ParticipantRole.DIRECTOR, ParticipantRole.CO_DIRECTOR) }
                    .map { participant ->
                        Triple(participant.person.publicId, "${participant.person.name} ${participant.person.lastname}", project.initialSubmission.year)
                    }
            }
            .filter { (professorId) ->
                professorIds == null || professorId in professorIds
            }
            .groupBy { (professorId, professorName, year) -> Pair(professorId, professorName) to year }
            .map { (key, items) ->
                val (professorPair, year) = key
                val (professorId, professorName) = professorPair
                ThesisTimelineData(
                    professorId = professorId.toString(),
                    professorName = professorName,
                    year = year,
                    count = items.size
                )
            }
            .sortedWith(compareBy({ it.professorName }, { it.year }))

        return ThesisTimelineResponse(data)
    }
}
