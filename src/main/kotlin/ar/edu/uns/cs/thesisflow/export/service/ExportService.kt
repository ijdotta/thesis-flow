package ar.edu.uns.cs.thesisflow.export.service

import ar.edu.uns.cs.thesisflow.export.dto.ExportFilters
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectType
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ProjectRepository
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ParticipantRole
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import java.util.*

@Service
class ExportService(
    private val projectRepository: ProjectRepository,
    private val objectMapper: ObjectMapper,
) {

    fun exportProjectsToCSV(filters: ExportFilters? = null): String {
        val projects = projectRepository.findAll()
        
        val careerUuids = filters?.careerIds?.mapNotNull { 
            runCatching { UUID.fromString(it) }.getOrNull() 
        }
        val domainUuids = filters?.applicationDomainIds?.mapNotNull { 
            runCatching { UUID.fromString(it) }.getOrNull() 
        }
        val tagUuids = filters?.tagIds?.mapNotNull { 
            runCatching { UUID.fromString(it) }.getOrNull() 
        }
        val professorUuids = filters?.professorIds?.mapNotNull { 
            runCatching { UUID.fromString(it) }.getOrNull() 
        }
        val projectTypes = filters?.projectTypes?.mapNotNull { typeStr ->
            runCatching { ProjectType.valueOf(typeStr.uppercase()) }.getOrNull()
        }

        val filteredProjects = projects
            .filter { project ->
                careerUuids == null || project.career?.publicId in careerUuids
            }
            .filter { project ->
                domainUuids == null || project.applicationDomain?.publicId in domainUuids
            }
            .filter { project ->
                tagUuids == null || project.tags.any { it.publicId in tagUuids }
            }
            .filter { project ->
                projectTypes == null || project.type in projectTypes
            }
            .filter { project ->
                val projectYear = project.initialSubmission.year
                (filters?.fromYear == null || projectYear >= filters.fromYear) &&
                (filters?.toYear == null || projectYear <= filters.toYear)
            }
            .filter { project ->
                if (professorUuids == null) true
                else project.participants.any { it.person.publicId in professorUuids }
            }

        return buildCSV(filteredProjects)
    }

    private fun buildCSV(projects: List<Any>): String {
        val csv = StringBuilder()
        
        // Header
        csv.append("publicId,title,type,subTypes,initialSubmission,completion,career,applicationDomain,tags,")
        csv.append("directors,coDirectors,collaborators,students,resources\n")

        // Rows
        @Suppress("UNCHECKED_CAST")
        for (project in projects as List<ar.edu.uns.cs.thesisflow.projects.persistance.entity.Project>) {
            csv.append(escapeCSV(project.publicId.toString())).append(",")
            csv.append(escapeCSV(project.title)).append(",")
            csv.append(escapeCSV(project.type.name)).append(",")
            csv.append(escapeCSV(project.subType.joinToString(";") { it.name })).append(",")
            csv.append(escapeCSV(project.initialSubmission.toString())).append(",")
            csv.append(escapeCSV(project.completion?.toString() ?: "")).append(",")
            csv.append(escapeCSV(project.career?.name ?: "")).append(",")
            csv.append(escapeCSV(project.applicationDomain?.name ?: "")).append(",")
            csv.append(escapeCSV(project.tags.joinToString(";") { it.name })).append(",")

            // Participants by role
            val directors = project.participants
                .filter { it.participantRole == ParticipantRole.DIRECTOR }
                .joinToString(";") { "${it.person.name} ${it.person.lastname}" }
            csv.append(escapeCSV(directors)).append(",")

            val coDirectors = project.participants
                .filter { it.participantRole == ParticipantRole.CO_DIRECTOR }
                .joinToString(";") { "${it.person.name} ${it.person.lastname}" }
            csv.append(escapeCSV(coDirectors)).append(",")

            val collaborators = project.participants
                .filter { it.participantRole == ParticipantRole.COLLABORATOR }
                .joinToString(";") { "${it.person.name} ${it.person.lastname}" }
            csv.append(escapeCSV(collaborators)).append(",")

            val students = project.participants
                .filter { it.participantRole == ParticipantRole.STUDENT }
                .joinToString(";") { "${it.person.name} ${it.person.lastname}" }
            csv.append(escapeCSV(students)).append(",")

            // Resources as JSON
            val resourcesJson = objectMapper.writeValueAsString(project.resources)
            csv.append(escapeCSV(resourcesJson)).append("\n")
        }

        return csv.toString()
    }

    private fun escapeCSV(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
