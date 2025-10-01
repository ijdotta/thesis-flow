package ar.edu.uns.cs.thesisflow.common

import java.util.UUID

/**
 * Centralized place for API related constants (paths, pagination defaults, etc.) and
 * common error message builders to avoid magic strings sprinkled across the code base.
 */
object ApiPaths {
    const val PEOPLE = "/people"
    const val STUDENTS = "/students"
    const val PROFESSORS = "/professors"
    const val CAREERS = "/careers"
}

object PaginationDefaults {
    const val PAGE = 0
    const val SIZE = 25
    const val PAGE_STRING = "0"
    const val SIZE_STRING = "25"
}

object CorsConfigConstants {
    const val PATH_PATTERN = "/**"
    // NOTE: When moving to other environments consider externalizing this property (e.g. application.yml)
    val ALLOWED_ORIGINS = arrayOf("http://localhost:5173")
    val ALLOWED_METHODS = arrayOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
}

object ValidationConstants {
    val PROFESSOR_VALID_EMAIL_DOMAINS = listOf("@cs.uns.edu.ar", "@uns.edu.ar")
}

object ErrorMessages {
    fun personNotFound(id: String?) = "Person $id does not exist"
    fun professorNotFound(id: String?) = "No professor found for $id"
    fun personAlreadyAssociated(personId: UUID) = "Person $personId is associated to other professor."
    fun emailNullOrBlank() = "Email cannot be null or blank."
    fun emailInvalidDomain(validDomains: Collection<String>) = "Email must end with '$validDomains'"
    fun noPersonForProfessor(personId: String?) = "No person found for $personId"
    fun studentNotFound(id: Any?) = "No student found for publicId: $id"
    fun personAlreadyStudent(personId: UUID) = "Person $personId already associated to other student"
    fun noPersonForStudent(personId: String?) = "No person found for publicId: $personId"
    fun someCareersDoNotExist(missing: Collection<UUID>) = "Some careers do not exist: $missing"
    fun careerNotFound(id: String?) = "Career with $id not found"
    fun careerNameBlank() = "Career name cannot be null or blank"
    fun projectNotFound(id: String?) = "Project not found for id $id"
    fun tagNotFound(id: String?) = "Tag not found for id $id"
    fun applicationDomainNotFound(id: String?) = "ApplicationDomain not found for id $id"
}
