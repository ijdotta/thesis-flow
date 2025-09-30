package ar.edu.uns.cs.thesisflow.projects.dto

import java.time.Instant

data class ProjectDTO(
    var publicId: String? = null,
    var title: String? = null,
    var type: String? = null,
    var subtype: List<String>? = null,
    var initialSubmission: Instant? = null,
    var completion: Instant? = null,
    var applicationDomainDTO: ApplicationDomainDTO? = null,
    var tags: List<TagDTO>? = null,
    var participants: List<ParticipantDTO>? = null,
)
