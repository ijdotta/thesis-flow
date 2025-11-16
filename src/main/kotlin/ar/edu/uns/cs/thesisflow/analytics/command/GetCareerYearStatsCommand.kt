package ar.edu.uns.cs.thesisflow.analytics.command

import ar.edu.uns.cs.thesisflow.analytics.dto.CareerYearStatsData
import ar.edu.uns.cs.thesisflow.analytics.dto.CareerYearStatsResponse
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectType
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ProjectRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
class GetCareerYearStatsCommand(
    private val projectRepository: ProjectRepository,
) {
    fun execute(
        careerIds: List<UUID>? = null,
        projectTypes: List<ProjectType>? = null,
        fromYear: Int? = null,
        toYear: Int? = null,
    ): CareerYearStatsResponse {
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
            .groupBy { Triple(it.career?.publicId ?: UUID.randomUUID(), it.career?.name ?: "Unknown", it.initialSubmission.year) }
            .map { (key, items) ->
                CareerYearStatsData(
                    careerId = key.first.toString(),
                    careerName = key.second,
                    year = key.third,
                    projectCount = items.size
                )
            }
            .sortedWith(compareBy({ it.careerName }, { it.year }))

        return CareerYearStatsResponse(data)
    }
}
