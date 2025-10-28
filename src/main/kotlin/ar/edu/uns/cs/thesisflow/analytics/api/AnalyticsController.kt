package ar.edu.uns.cs.thesisflow.analytics.api

import ar.edu.uns.cs.thesisflow.analytics.dto.*
import ar.edu.uns.cs.thesisflow.analytics.service.AnalyticsService
import ar.edu.uns.cs.thesisflow.projects.dto.ProjectDTO
import ar.edu.uns.cs.thesisflow.projects.service.ProjectService
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/analytics")
class AnalyticsController(
    private val analyticsService: AnalyticsService,
    private val projectService: ProjectService,
) {

    @GetMapping("/thesis-timeline")
    fun getThesisTimeline(
        @RequestParam(required = false) careerIds: String?,
        @RequestParam(required = false) professorIds: String?,
        @RequestParam(required = false) fromYear: Int?,
        @RequestParam(required = false) toYear: Int?,
    ): ResponseEntity<ThesisTimelineResponse> {
        val careerUuids = careerIds?.split(",")?.mapNotNull { runCatching { UUID.fromString(it.trim()) }.getOrNull() }
        val professorUuids = professorIds?.split(",")?.mapNotNull { runCatching { UUID.fromString(it.trim()) }.getOrNull() }
        
        return ResponseEntity.ok(
            analyticsService.getThesisTimeline(careerUuids, professorUuids, fromYear, toYear)
        )
    }

    @GetMapping("/topic-heatmap")
    fun getTopicHeatmap(
        @RequestParam(required = false) careerIds: String?,
        @RequestParam(required = false) fromYear: Int?,
        @RequestParam(required = false) toYear: Int?,
    ): ResponseEntity<TopicHeatmapResponse> {
        val careerUuids = careerIds?.split(",")?.mapNotNull { runCatching { UUID.fromString(it.trim()) }.getOrNull() }
        
        return ResponseEntity.ok(
            analyticsService.getTopicHeatmap(careerUuids, fromYear, toYear)
        )
    }

    @GetMapping("/professor-network")
    fun getProfessorNetwork(
        @RequestParam(required = false) careerIds: String?,
        @RequestParam(required = false) professorIds: String?,
        @RequestParam(required = false) fromYear: Int?,
        @RequestParam(required = false) toYear: Int?,
    ): ResponseEntity<ProfessorNetworkResponse> {
        val careerUuids = careerIds?.split(",")?.mapNotNull { runCatching { UUID.fromString(it.trim()) }.getOrNull() }
        val professorUuids = professorIds?.split(",")?.mapNotNull { runCatching { UUID.fromString(it.trim()) }.getOrNull() }
        
        return ResponseEntity.ok(
            analyticsService.getProfessorNetwork(careerUuids, professorUuids, fromYear, toYear)
        )
    }

    @GetMapping("/career-year-stats")
    fun getCareerYearStats(
        @RequestParam(required = false) careerIds: String?,
        @RequestParam(required = false) fromYear: Int?,
        @RequestParam(required = false) toYear: Int?,
    ): ResponseEntity<CareerYearStatsResponse> {
        val careerUuids = careerIds?.split(",")?.mapNotNull { runCatching { UUID.fromString(it.trim()) }.getOrNull() }
        
        return ResponseEntity.ok(
            analyticsService.getCareerYearStats(careerUuids, fromYear, toYear)
        )
    }

    @GetMapping("/filters")
    fun getFilters(): ResponseEntity<FiltersResponse> {
        return ResponseEntity.ok(analyticsService.getFilters())
    }

    @GetMapping("/project-type-stats")
    fun getProjectTypeStats(
        @RequestParam(required = false) careerIds: String?,
        @RequestParam(required = false) professorIds: String?,
        @RequestParam(required = false) fromYear: Int?,
        @RequestParam(required = false) toYear: Int?,
        @RequestParam(required = false) applicationDomainIds: String?,
    ): ResponseEntity<ProjectTypeStatsResponse> {
        val careerUuids = careerIds?.split(",")?.mapNotNull { runCatching { UUID.fromString(it.trim()) }.getOrNull() }
        val professorUuids = professorIds?.split(",")?.mapNotNull { runCatching { UUID.fromString(it.trim()) }.getOrNull() }
        val domainUuids = applicationDomainIds?.split(",")?.mapNotNull { runCatching { UUID.fromString(it.trim()) }.getOrNull() }

        return ResponseEntity.ok(
            analyticsService.getProjectTypeStats(careerUuids, professorUuids, fromYear, toYear, domainUuids)
        )
    }

    @GetMapping("/dashboard-stats")
    fun getDashboardStats(
        @RequestParam(required = false) careerIds: String?,
        @RequestParam(required = false) professorIds: String?,
        @RequestParam(required = false) fromYear: Int?,
        @RequestParam(required = false) toYear: Int?,
        @RequestParam(required = false) applicationDomainIds: String?,
        @RequestParam(required = false, defaultValue = "10") topK: Int,
    ): ResponseEntity<DashboardStatsResponse> {
        val careerUuids = careerIds?.split(",")?.mapNotNull { runCatching { UUID.fromString(it.trim()) }.getOrNull() }
        val professorUuids = professorIds?.split(",")?.mapNotNull { runCatching { UUID.fromString(it.trim()) }.getOrNull() }
        val domainUuids = applicationDomainIds?.split(",")?.mapNotNull { runCatching { UUID.fromString(it.trim()) }.getOrNull() }

        return ResponseEntity.ok(
            analyticsService.getDashboardStats(careerUuids, professorUuids, fromYear, toYear, domainUuids, topK)
        )
    }
}
