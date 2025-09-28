package ar.edu.uns.cs.thesisflow.catalog.dto

import ar.edu.uns.cs.thesisflow.catalog.persistance.entity.Career
import jakarta.validation.constraints.NotBlank

data class CareerDTO (
    val id: Long? = null,
    val publicId: String? = null,
    @field:NotBlank(message = "name is required")
    val name: String? = null,
) {
    fun toEntity() = Career(
        name = name!!
    )

    fun update(career: Career) {
        name?.let { career.name = it }
    }
}

fun Career.toDTO() = CareerDTO(
    id = id,
    publicId = publicId.toString(),
    name = name
)