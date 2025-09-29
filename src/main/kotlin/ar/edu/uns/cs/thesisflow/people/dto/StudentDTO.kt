package ar.edu.uns.cs.thesisflow.people.dto

import ar.edu.uns.cs.thesisflow.catalog.dto.CareerDTO
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Student
import ar.edu.uns.cs.thesisflow.people.validation.CreateStudent
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class StudentDTO(
    val publicId : String? = null,
    val personPublicId : String? = null,
    val person: PersonDTO? = null,
    val name: String? = null,
    val lastname: String? = null,
    @field:NotBlank(message = "studentId is required", groups = [CreateStudent::class])
    val studentId: String? = null,
    @field:Email(message = "email must be valid", groups = [CreateStudent::class])
    @field:NotBlank(message = "email is required", groups = [CreateStudent::class])
    val email: String? = null,
    val careers: List<CareerDTO> = emptyList(),
) {
    fun toEntity(person: Person) = Student(studentId = studentId!!, person = person, email = email!!)
    fun update(student: Student) {
        if (!studentId.isNullOrBlank()) {
            student.studentId = studentId
        }
        if (!email.isNullOrBlank()) {
            student.email = email
        }
    }
}

fun Student.toDTO(careers: List<CareerDTO> = listOf()) = StudentDTO(
    publicId = publicId.toString(),
    personPublicId = person?.publicId.toString(),
    person = person?.toDTO(),
    name = person?.name,
    lastname = person?.lastname,
    studentId = studentId,
    email = email,
    careers = careers
)