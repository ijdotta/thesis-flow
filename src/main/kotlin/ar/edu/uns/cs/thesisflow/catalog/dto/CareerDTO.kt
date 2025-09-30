package ar.edu.uns.cs.thesisflow.catalog.dto

import jakarta.validation.constraints.NotBlank

data class CareerDTO (
    var id: Long? = null,
    var publicId: String? = null,
    @field:NotBlank(message = "name is required")
    var name: String? = null,
)