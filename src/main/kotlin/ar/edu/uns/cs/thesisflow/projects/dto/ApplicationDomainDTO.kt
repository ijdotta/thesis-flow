package ar.edu.uns.cs.thesisflow.projects.dto

import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ApplicationDomain

data class ApplicationDomainDTO(
    var publicId: String? = null,
    var name: String? = null,
    var description: String? = null,
) {
    fun toEntity() = ApplicationDomain(name = name!!, description = description)
    fun update(entity: ApplicationDomain) {
        name?.let { entity.name = it }
        description?.let { entity.description = it }
    }
}

fun ApplicationDomain.toDTO() = ApplicationDomainDTO(
    publicId = publicId.toString(),
    name = name,
    description = description,
)
