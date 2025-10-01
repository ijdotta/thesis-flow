package ar.edu.uns.cs.thesisflow.people.dto

import ar.edu.uns.cs.thesisflow.people.validation.CreatePerson
import jakarta.validation.constraints.NotBlank
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person

data class PersonDTO(
    var id: Long? = null,
    var publicId: String? = null,
    @NotBlank(message = "name is required", groups = [CreatePerson::class])
    var name: String?,
    @NotBlank(message = "lastname is required", groups = [CreatePerson::class])
    var lastname: String?,
)

fun Person.toDTO() = PersonDTO(
    id = id,
    publicId = publicId.toString(),
    name = name,
    lastname = lastname,
)
