package ar.edu.uns.cs.thesisflow.analytics.command

import ar.edu.uns.cs.thesisflow.analytics.dto.ProjectTypeStatsData
import ar.edu.uns.cs.thesisflow.analytics.dto.ProjectTypeStatsResponse
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ParticipantRole
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectType
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ProjectRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
class GetProjectTypeStatsCommand(
    private val projectRepository: ProjectRepository,
) {
    fun execute(
        careerIds: List<UUID>? = null,
        professorIds: List<UUID>? = null,
        projectTypes: List<ProjectType>? = null,
        fromYear: Int? = null,
        toYear: Int? = null,
        applicationDomainIds: List<UUID>? = null,
    ): ProjectTypeStatsResponse {
        val filteredProjects = projectRepository.findAll()
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
            .filter { project ->
                applicationDomainIds == null || project.applicationDomain?.publicId in applicationDomainIds
            }
            .filter { project ->
                if (professorIds == null) true
                else project.participants.any { 
                    it.person.publicId in professorIds &&
                    it.participantRole in setOf(ParticipantRole.DIRECTOR, ParticipantRole.CO_DIRECTOR)
                }
            }

        val totalProjects = filteredProjects.size
        
        val data = filteredProjects
            .groupBy { it.type to it.initialSubmission.year }
            .map { (key, projects) ->
                val (type, year) = key
                val count = projects.size
                val percentage = if (totalProjects > 0) (count.toDouble() / totalProjects) * 100 else 0.0
                
                ProjectTypeStatsData(
                    projectType = type.name,
                    displayName = getProjectTypeDisplayName(type),
                    year = year,
                    projectCount = count,
                    percentage = String.format("%.1f", percentage).toDouble()
                )
            }
            .sortedWith(compareBy({ it.year }, { it.projectType }))

        return ProjectTypeStatsResponse(data)
    }

    private fun getProjectTypeDisplayName(type: ProjectType): String {
        return when (type) {
            ProjectType.THESIS -> "Tesis"
            ProjectType.FINAL_PROJECT -> "Trabajo Final"
        }
    }
}
