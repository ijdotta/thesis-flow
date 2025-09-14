package ar.edu.uns.cs.thesisflow.people.persistance.entity

import ar.edu.uns.cs.thesisflow.catalog.persistance.entity.Career
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(indexes = [Index(name = "public_id", columnList = "public_id")])
class StudentCareer(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,
    var publicId: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    var student: Student,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "career_id", nullable = false)
    var career: Career,
)