package ar.edu.uns.cs.thesisflow.analytics.service

import ar.edu.uns.cs.thesisflow.analytics.command.*
import ar.edu.uns.cs.thesisflow.analytics.dto.*
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
) {

    fun getThesisTimeline(
        careerIds: List<UUID>? = null,
        professorIds: List<UUID>? = null,
        fromYear: Int? = null,
        toYear: Int? = null,
    ): ThesisTimelineResponse {
        return getThesisTimelineCommand.execute(careerIds, professorIds, fromYear, toYear)
    }

    fun getTopicHeatmap(
        careerIds: List<UUID>? = null,
        fromYear: Int? = null,
        toYear: Int? = null,
    ): TopicHeatmapResponse {
        return getTopicHeatmapCommand.execute(careerIds, fromYear, toYear)
    }

    fun getProfessorNetwork(
        careerIds: List<UUID>? = null,
        professorIds: List<UUID>? = null,
        fromYear: Int? = null,
        toYear: Int? = null,
    ): ProfessorNetworkResponse {
        return getProfessorNetworkCommand.execute(careerIds, professorIds, fromYear, toYear)
    }

    fun getCareerYearStats(
        careerIds: List<UUID>? = null,
        fromYear: Int? = null,
        toYear: Int? = null,
    ): CareerYearStatsResponse {
        return getCareerYearStatsCommand.execute(careerIds, fromYear, toYear)
    }

    fun getFilters(): FiltersResponse {
        return getFiltersCommand.execute()
    }

    fun getProjectTypeStats(
        careerIds: List<UUID>? = null,
        professorIds: List<UUID>? = null,
        fromYear: Int? = null,
        toYear: Int? = null,
        applicationDomainIds: List<UUID>? = null,
    ): ProjectTypeStatsResponse {
        return getProjectTypeStatsCommand.execute(careerIds, professorIds, fromYear, toYear, applicationDomainIds)
    }

    fun getDashboardStats(
        careerIds: List<UUID>? = null,
        professorIds: List<UUID>? = null,
        fromYear: Int? = null,
        toYear: Int? = null,
        applicationDomainIds: List<UUID>? = null,
        topK: Int = 10,
    ): DashboardStatsResponse {
        return getDashboardStatsCommand.execute(careerIds, professorIds, fromYear, toYear, applicationDomainIds, topK)
    }
}
