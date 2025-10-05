package ar.edu.uns.cs.thesisflow.projects.api

import ar.edu.uns.cs.thesisflow.projects.dto.ProjectDTO
import ar.edu.uns.cs.thesisflow.projects.service.ProjectService
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ar.edu.uns.cs.thesisflow.projects.service.ProjectFilter
import ar.edu.uns.cs.thesisflow.projects.service.NullabilityFilter
import org.springframework.data.domain.Sort
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectType
import org.slf4j.LoggerFactory

@RestController
@RequestMapping("/projects")
class ProjectController(
    private val projectService: ProjectService
) {
    private val log = LoggerFactory.getLogger(ProjectController::class.java)

    private val sortableFields = setOf(
        "createdAt", "updatedAt", "title", "type", "initialSubmission", "completion"
    )
    private val pseudoFieldMapping = mapOf(
        // Pseudo fields mapped to a real column (placeholder until implemented properly)
        "students" to "createdAt",
        "directors" to "createdAt"
    )

    @GetMapping
    fun findAll(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "25") size: Int,
        @RequestParam(required = false) title: String?,
        @RequestParam(name = "professor.name", required = false) professorName: String?,
        @RequestParam(required = false) directors: String?, // alias for professorName
        @RequestParam(name = "student.name", required = false) studentName: String?,
        @RequestParam(required = false) students: String?, // alias for studentName
        @RequestParam(required = false) domain: String?,
        @RequestParam(required = false) completed: Boolean?, // legacy alias
        @RequestParam(required = false) completion: Boolean?, // preferred param (true -> completion NOT NULL)
        @RequestParam(required = false) type: String?, // project type filter (supports synonyms & comma-separated)
        @RequestParam(required = false) sort: String?, // e.g. createdAt,desc OR students,asc (pseudo)
    ): ResponseEntity<*> {
        val effectiveProfessor = (directors ?: professorName)?.takeIf { it.isNotBlank() }
        val effectiveStudent = (students ?: studentName)?.takeIf { it.isNotBlank() }
        val completionFlag = completion ?: completed // prefer 'completion'
        val normalizedType = normalizeTypeParam(type)

        val filter = ProjectFilter(
            title = title?.takeIf { it.isNotBlank() },
            professorName = effectiveProfessor,
            studentName = effectiveStudent,
            domain = domain?.takeIf { it.isNotBlank() },
            completion = completionFlag.toNullabilityFilter(),
            type = normalizedType,
        )

        val pageable = PageRequest.of(page, size, sort.toSort())

        log.debug(
            "Project filter request -> page={}, size={}, title='{}', directors='{}', students='{}', domain='{}', completionFlag={}, typeRaw='{}', typeNormalized='{}', sort='{}'",
            page, size, title, effectiveProfessor, effectiveStudent, domain, completionFlag, type, normalizedType, sort
        )

        return ResponseEntity.ok(projectService.findAll(pageable, filter))
    }

    private fun String?.toSort(): Sort {
        if (this.isNullOrBlank()) return Sort.unsorted()
        val parts = this.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (parts.isEmpty()) return Sort.unsorted()
        val originalField = parts[0]
        val field = pseudoFieldMapping[originalField] ?: originalField
        if (field != originalField) {
            log.warn("Remapped unsupported sort field '{}' to '{}' (placeholder mapping)", originalField, field)
        }
        if (field !in sortableFields) {
            log.warn("Ignoring unsupported sort field '{}' (allowed: {})", field, sortableFields.joinToString(","))
            return Sort.unsorted()
        }
        val direction = if (parts.size > 1) parts[1].lowercase() else "asc"
        val dir = if (direction == "desc") Sort.Direction.DESC else Sort.Direction.ASC
        return Sort.by(Sort.Order(dir, field))
    }

    private fun Boolean?.toNullabilityFilter(): NullabilityFilter? = when (this) {
        null -> null
        true -> NullabilityFilter.NOT_NULL
        false -> NullabilityFilter.NULL
    }

    // Map incoming type param to canonical enum names, supporting synonyms & multiple values
    private fun normalizeTypeParam(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val synonyms = mapOf(
            "PROJECT" to ProjectType.FINAL_PROJECT.name,
            "FINALPROJECT" to ProjectType.FINAL_PROJECT.name,
            "FINAL-PROJECT" to ProjectType.FINAL_PROJECT.name,
            "FINAL_PROYECTO" to ProjectType.FINAL_PROJECT.name, // potential localization
            "TESIS" to ProjectType.THESIS.name // Spanish variant
        )
        val canon = raw.split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { it.uppercase() }
            .map { synonyms[it] ?: it }
            .mapNotNull { value -> runCatching { ProjectType.valueOf(value).name }.getOrNull() }
            .distinct()
        return if (canon.isEmpty()) null else canon.joinToString(",")
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: String) = ResponseEntity.ok(projectService.findByPublicId(id))

    @PostMapping
    fun create(@RequestBody projectDTO: ProjectDTO) = projectService.create(projectDTO)

    @PutMapping("/{id}")
    fun update(@PathVariable id: String, @RequestBody projectDTO: ProjectDTO) =
        projectService.update(id, projectDTO)

    @PutMapping("/{id}/tags")
    fun setTags(@PathVariable id: String, @RequestBody setTagsRequest: SetTagsRequest) =
        projectService.setTags(id, setTagsRequest.tagIds)

    @PutMapping("/{id}/application-domain")
    fun setApplicationDomain(
        @PathVariable id: String,
        @RequestBody setApplicationDomainRequest: SetApplicationDomainRequest
    ) = projectService.setApplicationDomain(id, setApplicationDomainRequest.applicationDomainId)

    @PutMapping("/{id}/participants")
    fun setParticipants(
        @PathVariable id: String,
        @RequestBody setParticipantsRequest: SetParticipantsRequest
    ) = projectService.setParticipants(id, setParticipantsRequest.participants)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String) = projectService.delete(id)
}
