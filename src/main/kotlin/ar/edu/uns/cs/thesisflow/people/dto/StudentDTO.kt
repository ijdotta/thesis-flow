package ar.edu.uns.cs.thesisflow.people.dto

import ar.edu.uns.cs.thesisflow.catalog.dto.CareerDTO
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Student

data class StudentDTO(
    val publicId : String? = null,
    val personPublicId : String? = null,
    val person: PersonDTO? = null,
    val name: String? = null,
    val lastname: String? = null,
    val studentId: String? = null,
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