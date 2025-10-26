package ar.edu.uns.cs.thesisflow.analytics.dto

import java.util.UUID

data class ThesisTimelineData(
    val professorId: String,
    val professorName: String,
    val year: Int,
    val count: Int
)

data class ThesisTimelineResponse(
    val data: List<ThesisTimelineData>
)

data class TopicHeatmapData(
    val topic: String,
    val year: Int,
    val count: Int
)

data class TopicHeatmapResponse(
    val data: List<TopicHeatmapData>
)

data class NetworkNode(
    val id: String,
    val name: String,
    val projectCount: Int
)

data class NetworkEdge(
    val source: String,
    val target: String,
    val weight: Int,
    val collaborations: Int
)

data class ProfessorNetworkResponse(
    val nodes: List<NetworkNode>,
    val edges: List<NetworkEdge>
)

data class CareerYearStatsData(
    val careerId: String,
    val careerName: String,
    val year: Int,
    val projectCount: Int
)

data class CareerYearStatsResponse(
    val data: List<CareerYearStatsData>
)

data class FilterOption(
    val id: String,
    val name: String
)

data class YearRange(
    val minYear: Int,
    val maxYear: Int
)

data class FiltersResponse(
    val careers: List<FilterOption>,
    val professors: List<FilterOption>,
    val yearRange: YearRange
)
