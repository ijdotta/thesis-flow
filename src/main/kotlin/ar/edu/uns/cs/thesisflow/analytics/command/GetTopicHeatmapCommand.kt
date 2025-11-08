package ar.edu.uns.cs.thesisflow.analytics.command

import ar.edu.uns.cs.thesisflow.analytics.dto.TopicHeatmapData
import ar.edu.uns.cs.thesisflow.analytics.dto.TopicHeatmapResponse
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ProjectRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
class GetTopicHeatmapCommand(
    private val projectRepository: ProjectRepository,
) {
    fun execute(
        careerIds: List<UUID>? = null,
        fromYear: Int? = null,
        toYear: Int? = null,
    ): TopicHeatmapResponse {
        val projects = projectRepository.findAll()
            .filter { project ->
                careerIds == null || project.career?.publicId in careerIds
            }
            .filter { project ->
                val projectYear = project.initialSubmission.year
                (fromYear == null || projectYear >= fromYear) &&
                (toYear == null || projectYear <= toYear)
            }

        val data = projects
            .flatMap { project ->
                project.tags.map { tag ->
                    Triple(tag.name, project.initialSubmission.year, project.publicId)
                }
            }
            .groupBy { (topic, year) -> topic to year }
            .map { (key, items) ->
                val (topic, year) = key
                TopicHeatmapData(
                    topic = topic,
                    year = year,
                    count = items.map { it.third }.distinct().size
                )
            }
            .sortedWith(compareBy({ it.topic }, { it.year }))

        return TopicHeatmapResponse(data)
    }
}
