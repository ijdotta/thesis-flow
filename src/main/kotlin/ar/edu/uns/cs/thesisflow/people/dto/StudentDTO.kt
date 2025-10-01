package ar.edu.uns.cs.thesisflow.people.dto

import ar.edu.uns.cs.thesisflow.catalog.dto.CareerDTO
import ar.edu.uns.cs.thesisflow.people.validation.CreateStudent
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class StudentDTO(
    var publicId : String? = null,
    var personPublicId : String? = null,
    var person: PersonDTO? = null,
    var name: String? = null,
    var lastname: String? = null,
    @field:NotBlank(message = "studentId is required", groups = [CreateStudent::class])
    var studentId: String? = null,
    @field:Email(message = "email must be valid", groups = [CreateStudent::class])
    @field:NotBlank(message = "email is required", groups = [CreateStudent::class])
    var email: String? = null,
    var careers: List<CareerDTO> = emptyList(),
)