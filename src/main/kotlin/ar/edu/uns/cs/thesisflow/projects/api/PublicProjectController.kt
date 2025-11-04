package ar.edu.uns.cs.thesisflow.projects.api

import ar.edu.uns.cs.thesisflow.projects.dto.ProjectDTO
import ar.edu.uns.cs.thesisflow.projects.dto.toDTO
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ParticipantRole
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectType
import ar.edu.uns.cs.thesisflow.projects.service.ProjectService
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/projects/public")
class PublicProjectController(
    private val projectService: ProjectService,
) {

    @GetMapping
    fun getPublicProjects(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "20") size: Int,
        @RequestParam(required = false) careerIds: String?,
        @RequestParam(required = false) professorIds: String?,
        @RequestParam(required = false) projectTypeIds: String?,
        @RequestParam(required = false) fromYear: Int?,
        @RequestParam(required = false) toYear: Int?,
        @RequestParam(required = false) search: String?,
    ): ResponseEntity<*> {
        val pageable = PageRequest.of(page, size)
        val careerUuids = careerIds?.split(",")?.mapNotNull { runCatching { UUID.fromString(it.trim()) }.getOrNull() }
        val professorUuids = professorIds?.split(",")?.mapNotNull { runCatching { UUID.fromString(it.trim()) }.getOrNull() }
        val projectTypes = projectTypeIds?.split(",")?.map { it.trim().uppercase() }?.mapNotNull { typeStr ->
            runCatching { ProjectType.valueOf(typeStr) }.getOrNull()
        }

        val allProjects = projectService.getAll()
            .filter { project ->
                careerUuids == null || project.career?.publicId in careerUuids
            }
            .filter { project ->
                projectTypes == null || project.type in projectTypes
            }
            .filter { project ->
                professorUuids == null || project.participants.any { participant ->
                    participant.person.publicId in professorUuids &&
                    participant.participantRole in setOf(ParticipantRole.DIRECTOR, ParticipantRole.CO_DIRECTOR)
                }
            }
            .filter { project ->
                val projectYear = project.initialSubmission.year
                (fromYear == null || projectYear >= fromYear) &&
                (toYear == null || projectYear <= toYear)
            }
            .filter { project ->
                search == null || 
                project.title.contains(search, ignoreCase = true) ||
                project.tags.any { tag -> tag.name.contains(search, ignoreCase = true) }
            }

        val total = allProjects.size
        val startIdx = page * size
        val endIdx = minOf(startIdx + size, total)
        val paginatedProjects = if (startIdx < total) {
            allProjects.subList(startIdx, endIdx)
        } else {
            emptyList()
        }

        val projectDTOs = paginatedProjects.map { project ->
            val participants = project.participants.map { participant ->
                ar.edu.uns.cs.thesisflow.projects.dto.ParticipantDTO(
                    role = participant.participantRole.name,
                    personDTO = ar.edu.uns.cs.thesisflow.people.dto.PersonDTO(
                        id = participant.person.id,
                        publicId = participant.person.publicId.toString(),
                        name = participant.person.name,
                        lastname = participant.person.lastname,
                    )
                )
            }
            project.toDTO(participants)
        }

        val result = PageImpl(projectDTOs, pageable, total.toLong())
        return ResponseEntity.ok(mapOf(
            "content" to result.content,
            "totalElements" to result.totalElements,
            "totalPages" to result.totalPages,
            "currentPage" to result.number,
            "size" to result.size
        ))
    }
}
