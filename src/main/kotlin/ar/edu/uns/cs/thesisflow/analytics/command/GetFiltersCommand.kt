package ar.edu.uns.cs.thesisflow.analytics.command

import ar.edu.uns.cs.thesisflow.analytics.dto.FilterOption
import ar.edu.uns.cs.thesisflow.analytics.dto.FiltersResponse
import ar.edu.uns.cs.thesisflow.analytics.dto.YearRange
import ar.edu.uns.cs.thesisflow.catalog.persistance.repository.CareerRepository
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ParticipantRole
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ProjectRepository
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class GetFiltersCommand(
    private val projectRepository: ProjectRepository,
    private val careerRepository: CareerRepository,
) {
    fun execute(): FiltersResponse {
        val allProjects = projectRepository.findAll()
        
        val careers = careerRepository.findAll()
            .map { FilterOption(it.publicId.toString(), it.name) }
            .sortedBy { it.name }

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

        val projectTypes = listOf(
            FilterOption("THESIS", "Tesis"),
            FilterOption("FINAL_PROJECT", "Proyecto Final")
        )

        return FiltersResponse(careers, professors, yearRange, projectTypes)
    }
}
