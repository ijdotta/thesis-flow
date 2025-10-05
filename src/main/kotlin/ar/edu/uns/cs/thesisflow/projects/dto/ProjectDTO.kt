package ar.edu.uns.cs.thesisflow.projects.dto

import ar.edu.uns.cs.thesisflow.projects.persistance.entity.Project
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectSubType
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectType
import java.time.LocalDate

data class ProjectDTO(
    val publicId: String? = null,
    val title: String? = null,
    val type: String? = null,
    val subtype: List<String>? = null,
    val initialSubmission: LocalDate? = null,
    val completion: LocalDate? = null,
    val applicationDomainDTO: ApplicationDomainDTO? = null,
    val tags: List<TagDTO>? = null,
    val participants: List<ParticipantDTO>? = null,
) {
    fun toEntity() = Project(
        title = title!!,
        type = ProjectType.valueOf(type!!),
        subType = subtype?.map { ProjectSubType.valueOf(it) }?.toMutableSet() ?: mutableSetOf(),
        initialSubmission = initialSubmission!!,
        completion = completion,
    )

    fun update(project: Project) {
        title?.let { project.title = it }
        type?.let { project.type = ProjectType.valueOf(it) }
        subtype?.let { project.subType = subtype.map { ProjectSubType.valueOf(it) }.toMutableSet() }
        initialSubmission?.let { project.initialSubmission = it }
        completion?.let { project.completion = it }
    }
}

fun Project.toDTO(participantDTOs: List<ParticipantDTO> = listOf()) = ProjectDTO(
    publicId = this.publicId.toString(),
    title = this.title,
    type = this.type.name,
    subtype = this.subType.map { it.name }.toList(),
    initialSubmission = initialSubmission,
    completion = completion,
    applicationDomainDTO = applicationDomain?.toDTO(),
    tags = tags.map { it.toDTO() },
    participants = participantDTOs
)
