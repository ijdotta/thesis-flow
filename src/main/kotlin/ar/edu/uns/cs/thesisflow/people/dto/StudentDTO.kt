package ar.edu.uns.cs.thesisflow.people.dto

import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Student

data class StudentDTO(
    val publicId : String?,
    val personPublicId : String?,
    val name: String?,
    val lastname: String?,
    val studentId: String?,
    val email: String?,
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

fun Student.toDTO() = StudentDTO(
    publicId = publicId.toString(),
    personPublicId = person?.publicId.toString(),
    name = person?.name,
    lastname = person?.name,
    studentId = studentId,
    email = email,
)