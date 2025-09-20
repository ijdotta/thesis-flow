package ar.edu.uns.cs.thesisflow.projects.dto

import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ApplicationDomain

data class ApplicationDomainDTO(
    val publicId: String? = null,
    val name: String? = null,
    val description: String? = null,
) {
    fun toEntity() = ApplicationDomain(
        name = name!!,
        description = description,
    )

    fun update(applicationDomain: ApplicationDomain) {
        name?.let { applicationDomain.name = it }
        description?.let { applicationDomain.description = it }
    }
}

fun ApplicationDomain.toDTO() = ApplicationDomainDTO(
    publicId = publicId.toString(),
    name = name,
    description = description,
)
