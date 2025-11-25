package ar.edu.uns.cs.thesisflow.analytics.service

import ar.edu.uns.cs.thesisflow.analytics.command.*
import ar.edu.uns.cs.thesisflow.analytics.dto.*
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectType
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ParticipantRole
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ProjectRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class AnalyticsService(
    private val getThesisTimelineCommand: GetThesisTimelineCommand,
    private val getTopicHeatmapCommand: GetTopicHeatmapCommand,
    private val getProfessorNetworkCommand: GetProfessorNetworkCommand,
    private val getCareerYearStatsCommand: GetCareerYearStatsCommand,
    private val getFiltersCommand: GetFiltersCommand,
    private val getProjectTypeStatsCommand: GetProjectTypeStatsCommand,
    private val getDashboardStatsCommand: GetDashboardStatsCommand,
    private val projectRepository: ProjectRepository,
) {

    fun getThesisTimeline(
        careerIds: List<UUID>? = null,
        professorIds: List<UUID>? = null,
        projectTypes: List<ProjectType>? = null,
        fromYear: Int? = null,
        toYear: Int? = null,
    ): ThesisTimelineResponse {
        return getThesisTimelineCommand.execute(careerIds, professorIds, projectTypes, fromYear, toYear)
    }

    fun getTopicHeatmap(
        careerIds: List<UUID>? = null,
        professorIds: List<UUID>? = null,
        projectTypes: List<ProjectType>? = null,
        fromYear: Int? = null,
        toYear: Int? = null,
    ): TopicHeatmapResponse {
        return getTopicHeatmapCommand.execute(careerIds, professorIds, projectTypes, fromYear, toYear)
    }

    fun getProfessorNetwork(
        careerIds: List<UUID>? = null,
        professorIds: List<UUID>? = null,
        projectTypes: List<ProjectType>? = null,
        fromYear: Int? = null,
        toYear: Int? = null,
    ): ProfessorNetworkResponse {
        return getProfessorNetworkCommand.execute(careerIds, professorIds, projectTypes, fromYear, toYear)
    }

    fun getCareerYearStats(
        careerIds: List<UUID>? = null,
        professorIds: List<UUID>? = null,
        projectTypes: List<ProjectType>? = null,
        fromYear: Int? = null,
        toYear: Int? = null,
    ): CareerYearStatsResponse {
        return getCareerYearStatsCommand.execute(careerIds, professorIds, projectTypes, fromYear, toYear)
    }

    fun getFilters(): FiltersResponse {
        return getFiltersCommand.execute()
    }

    fun getProjectTypeStats(
        careerIds: List<UUID>? = null,
        professorIds: List<UUID>? = null,
        projectTypes: List<ProjectType>? = null,
        fromYear: Int? = null,
        toYear: Int? = null,
        applicationDomainIds: List<UUID>? = null,
    ): ProjectTypeStatsResponse {
        return getProjectTypeStatsCommand.execute(careerIds, professorIds, projectTypes, fromYear, toYear, applicationDomainIds)
    }

    fun getDashboardStats(
        careerIds: List<UUID>? = null,
        professorIds: List<UUID>? = null,
        projectTypes: List<ProjectType>? = null,
        fromYear: Int? = null,
        toYear: Int? = null,
        applicationDomainIds: List<UUID>? = null,
        topK: Int = 10,
    ): DashboardStatsResponse {
        return getDashboardStatsCommand.execute(careerIds, professorIds, projectTypes, fromYear, toYear, applicationDomainIds, topK)
    }

    fun getProfessorStats(
        professorIds: List<UUID>? = null,
        topK: Int = 10,
    ): DashboardStatsResponse {
        if (professorIds == null || professorIds.isEmpty()) {
            return DashboardStatsResponse(
                overview = OverviewStats(0, 0, 0, 0, 0, 0),
                topDomains = emptyList(),
                topTags = emptyList(),
                topProfessors = emptyList()
            )
        }

        val allProjects = projectRepository.findAll()
        val filteredProjects = allProjects.filter { project ->
            project.participants.any { 
                it.person.publicId in professorIds && 
                it.participantRole in setOf(ParticipantRole.DIRECTOR, ParticipantRole.CO_DIRECTOR, ParticipantRole.COLLABORATOR)
            }
        }

        val overview = OverviewStats(
            totalProjects = allProjects.size,
            filteredProjects = filteredProjects.size,
            uniqueDomains = filteredProjects.mapNotNull { it.applicationDomain?.publicId }.distinct().size,
            uniqueTags = filteredProjects.flatMap { it.tags }.map { it.publicId }.distinct().size,
            uniqueProfessors = filteredProjects
                .flatMap { it.participants }
                .filter { it.participantRole in setOf(ParticipantRole.DIRECTOR, ParticipantRole.CO_DIRECTOR, ParticipantRole.COLLABORATOR) }
                .map { it.person.publicId }
                .distinct()
                .size,
            projectsWithAccessibleUrl = filteredProjects.count { it.resources.isNotEmpty() }
        )

        val topDomains = filteredProjects
            .groupBy { it.applicationDomain?.publicId to it.applicationDomain?.name }
            .filter { it.key.first != null }
            .map { (domain, projects) -> TopItemData(domain.first.toString(), domain.second!!, projects.size) }
            .sortedByDescending { it.count }
            .take(topK)

        val topTags = filteredProjects
            .flatMap { project -> project.tags.map { tag -> tag to project } }
            .groupBy { (tag, _) -> tag.publicId to tag.name }
            .map { (tag, items) -> TopItemData(tag.first.toString(), tag.second, items.size) }
            .sortedByDescending { it.count }
            .take(topK)

        return DashboardStatsResponse(
            overview = overview,
            topDomains = topDomains,
            topTags = topTags,
            topProfessors = emptyList()
        )
    }
}
