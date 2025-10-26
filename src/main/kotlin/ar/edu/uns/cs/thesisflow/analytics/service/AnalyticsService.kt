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
            val projectProfessors = project.participants.map { it.person.publicId to "${it.person.name} ${it.person.lastname}" }
            
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

        val professors = allProjects
            .flatMap { it.participants }
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

        return FiltersResponse(careers, professors, yearRange)
    }
}
