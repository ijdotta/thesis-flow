package ar.edu.uns.cs.thesisflow.catalog.dto

import ar.edu.uns.cs.thesisflow.catalog.persistance.entity.Career
import java.util.UUID

data class CareerDTO (
    val id: Long? = null,
    val publicId: String? = null,
    val name: String? = null,
) {
    fun toEntity() = Career(
        publicId = publicId?.let { UUID.fromString(it) },
        name = name!!
    )

    fun update(career: Career) {
        name?.let { career.name = it }
    }
}

fun Career.toDTO() = CareerDTO(
    publicId = publicId.toString(),
    name = name
)