package ar.edu.uns.cs.thesisflow.analytics.command

import ar.edu.uns.cs.thesisflow.analytics.dto.DashboardStatsResponse
import ar.edu.uns.cs.thesisflow.analytics.dto.OverviewStats
import ar.edu.uns.cs.thesisflow.analytics.dto.TopItemData
import ar.edu.uns.cs.thesisflow.analytics.dto.TopProfessorData
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ParticipantRole
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ProjectRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
class GetDashboardStatsCommand(
    private val projectRepository: ProjectRepository,
) {
    fun execute(
        careerIds: List<UUID>? = null,
        professorIds: List<UUID>? = null,
        fromYear: Int? = null,
        toYear: Int? = null,
        applicationDomainIds: List<UUID>? = null,
        topK: Int = 10,
    ): DashboardStatsResponse {
        val allProjects = projectRepository.findAll()
        val filteredProjects = allProjects
            .filter { project ->
                careerIds == null || project.career?.publicId in careerIds
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

        val overview = OverviewStats(
            totalProjects = allProjects.size,
            filteredProjects = filteredProjects.size,
            uniqueDomains = filteredProjects.mapNotNull { it.applicationDomain?.publicId }.distinct().size,
            uniqueTags = filteredProjects.flatMap { it.tags }.map { it.publicId }.distinct().size,
            uniqueProfessors = filteredProjects
                .flatMap { it.participants }
                .filter { it.participantRole in setOf(ParticipantRole.DIRECTOR, ParticipantRole.CO_DIRECTOR) }
                .map { it.person.publicId }
                .distinct()
                .size,
            projectsWithAccessibleUrl = filteredProjects.count { 
                it.resources != null && it.resources.trim() != "[]" && it.resources.trim().isNotEmpty()
            }
        )

        val topDomains = filteredProjects
            .mapNotNull { it.applicationDomain }
            .groupingBy { it.publicId to it.name }
            .eachCount()
            .map { (key, count) ->
                TopItemData(id = key.first.toString(), name = key.second, count = count)
            }
            .sortedByDescending { it.count }
            .take(minOf(topK, 20))

        val topTags = filteredProjects
            .flatMap { it.tags }
            .groupingBy { it.publicId to it.name }
            .eachCount()
            .map { (key, count) ->
                TopItemData(id = key.first.toString(), name = key.second, count = count)
            }
            .sortedByDescending { it.count }
            .take(minOf(topK, 20))

        val topProfessors = filteredProjects
            .flatMap { it.participants }
            .filter { it.participantRole in setOf(ParticipantRole.DIRECTOR, ParticipantRole.CO_DIRECTOR) }
            .groupingBy { it.person.publicId to "${it.person.name} ${it.person.lastname}" }
            .eachCount()
            .map { (key, count) ->
                TopProfessorData(id = key.first.toString(), name = key.second, projectCount = count)
            }
            .sortedByDescending { it.projectCount }
            .take(minOf(topK, 20))

        return DashboardStatsResponse(
            overview = overview,
            topDomains = topDomains,
            topTags = topTags,
            topProfessors = topProfessors
        )
    }
}
