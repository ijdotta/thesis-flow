package ar.edu.uns.cs.thesisflow.backup.dto

import java.time.LocalDate
import java.util.UUID

/**
 * DTOs for backup/restore operations.
 * These are flattened representations without circular references.
 * Public IDs are used to maintain relationships during restore.
 */

data class CareerBackupDto(
    val id: Long?,
    val publicId: UUID,
    val name: String,
    val description: String?,
)

data class PersonBackupDto(
    val id: Long?,
    val publicId: UUID,
    val name: String,
    val lastname: String,
    val email: String,
)

data class ProfessorBackupDto(
    val id: Long?,
    val publicId: UUID,
    val email: String,
    val personPublicId: UUID, // Reference to Person
)

data class StudentBackupDto(
    val id: Long?,
    val publicId: UUID,
    val studentId: String,
    val email: String,
    val personPublicId: UUID, // Reference to Person
)

data class StudentCareerBackupDto(
    val id: Long?,
    val publicId: UUID,
    val studentPublicId: UUID, // Reference to Student
    val careerPublicId: UUID, // Reference to Career
)

data class ProfessorLoginTokenBackupDto(
    val id: Long?,
    val publicId: UUID,
    val professorPublicId: UUID, // Reference to Professor
    val tokenValue: String,
    val expiresAt: LocalDate,
)

data class ApplicationDomainBackupDto(
    val id: Long?,
    val publicId: UUID,
    val name: String,
    val description: String?,
)

data class TagBackupDto(
    val id: Long?,
    val publicId: UUID,
    val name: String,
    val description: String?,
)

data class ProjectBackupDto(
    val id: Long?,
    val publicId: UUID,
    val title: String,
    val description: String?,
    val type: String, // ProjectType enum name
    val initialSubmission: LocalDate?,
    val completion: LocalDate?,
    val careerPublicId: UUID?, // Reference to Career
    val applicationDomainPublicId: UUID?, // Reference to ApplicationDomain
    val resources: String = "[]", // JSON string of resources
)

data class ProjectParticipantBackupDto(
    val id: Long?,
    val publicId: UUID,
    val projectPublicId: UUID, // Reference to Project
    val personPublicId: UUID, // Reference to Person
    val participantRole: String, // ParticipantRole enum name
)
