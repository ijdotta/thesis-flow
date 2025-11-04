package ar.edu.uns.cs.thesisflow.analytics.service

import ar.edu.uns.cs.thesisflow.analytics.dto.*
import ar.edu.uns.cs.thesisflow.catalog.persistance.repository.CareerRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.PersonRepository
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ParticipantRole
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectType
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ProjectRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.*

@Service
class AnalyticsService(
    private val projectRepository: ProjectRepository,
    private val careerRepository: CareerRepository,
    private val personRepository: PersonRepository,
) {

    fun getThesisTimeline(
        careerIds: List<UUID>? = null,
        professorIds: List<UUID>? = null,
        fromYear: Int? = null,
        toYear: Int? = null,
    ): ThesisTimelineResponse {
        val projects = projectRepository.findAll()
            .filter { it.type == ProjectType.THESIS }
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

    fun getTopicHeatmap(
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

    fun getProfessorNetwork(
        careerIds: List<UUID>? = null,
        professorIds: List<UUID>? = null,
        fromYear: Int? = null,
        toYear: Int? = null,
    ): ProfessorNetworkResponse {
        val projects = projectRepository.findAll()
            .filter { project ->
                careerIds == null || project.career?.publicId in careerIds
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

    fun getCareerYearStats(
        careerIds: List<UUID>? = null,
        fromYear: Int? = null,
        toYear: Int? = null,
    ): CareerYearStatsResponse {
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

    fun getFilters(): FiltersResponse {
        val allProjects = projectRepository.findAll()
        
        val careers = careerRepository.findAll()
            .map { FilterOption(it.publicId.toString(), it.name) }
            .sortedBy { it.name }

        // FIX: Only return professors with DIRECTOR or CO_DIRECTOR roles
        val professors = allProjects
            .flatMap { it.participants }
            .filter { it.participantRole in setOf(ParticipantRole.DIRECTOR, ParticipantRole.CO_DIRECTOR) }
            .map { it.person }
            .distinctBy { it.publicId }
            .map { FilterOption(it.publicId.toString(), "${it.name} ${it.lastname}") }
            .sortedBy { it.name }

        val years = allProjects.map { it.initialSubmission.year }
        val yearRange = if (years.isNotEmpty()) {
            YearRange(minYear = years.minOrNull() ?: 2010, maxYear = years.maxOrNull() ?: 2025)
        } else {
            YearRange(minYear = 2010, maxYear = LocalDate.now().year)
        }

        // Project types
        val projectTypes = listOf(
            FilterOption("THESIS", "Tesis"),
            FilterOption("FINAL_PROJECT", "Proyecto Final")
        )

        return FiltersResponse(careers, professors, yearRange, projectTypes)
    }

    fun getProjectTypeStats(
        careerIds: List<UUID>? = null,
        professorIds: List<UUID>? = null,
        fromYear: Int? = null,
        toYear: Int? = null,
        applicationDomainIds: List<UUID>? = null,
    ): ProjectTypeStatsResponse {
        val filteredProjects = projectRepository.findAll()
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

    fun getDashboardStats(
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

        // Overview statistics
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

        // Top domains
        val topDomains = filteredProjects
            .mapNotNull { it.applicationDomain }
            .groupingBy { it.publicId to it.name }
            .eachCount()
            .map { (key, count) ->
                TopItemData(id = key.first.toString(), name = key.second, count = count)
            }
            .sortedByDescending { it.count }
            .take(minOf(topK, 20))

        // Top tags
        val topTags = filteredProjects
            .flatMap { it.tags }
            .groupingBy { it.publicId to it.name }
            .eachCount()
            .map { (key, count) ->
                TopItemData(id = key.first.toString(), name = key.second, count = count)
            }
            .sortedByDescending { it.count }
            .take(minOf(topK, 20))

        // Top professors
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

    private fun getProjectTypeDisplayName(type: ProjectType): String {
        return when (type) {
            ProjectType.THESIS -> "Tesis"
            ProjectType.FINAL_PROJECT -> "Trabajo Final"
        }
    }
}
