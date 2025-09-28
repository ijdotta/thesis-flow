package ar.edu.uns.cs.thesisflow.people.persistance.entity

import ar.edu.uns.cs.thesisflow.catalog.persistance.entity.Career
import ar.edu.uns.cs.thesisflow.common.persistence.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    indexes = [Index(name = "idx_student_career_public_id", columnList = "public_id")],
    uniqueConstraints = [UniqueConstraint(name = "uc_student_career", columnNames = ["student_id", "career_id"])]
)
class StudentCareer(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    var student: Student? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "career_id", nullable = false)
    var career: Career? = null,
) : BaseEntity()
