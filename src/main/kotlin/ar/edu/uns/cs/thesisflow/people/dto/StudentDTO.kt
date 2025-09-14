package ar.edu.uns.cs.thesisflow.people.dto

import ar.edu.uns.cs.thesisflow.catalog.dto.CareerDTO
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Student

data class StudentDTO(
    val publicId : String?,
    val personPublicId : String?,
    val person: PersonDTO?,
    val studentId: String?,
    val email: String?,
    val careers: List<CareerDTO>,
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
    studentId = studentId,
    email = email,
    careers = careers
)