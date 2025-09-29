package ar.edu.uns.cs.thesisflow.people.persistance.entity

import ar.edu.uns.cs.thesisflow.common.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(indexes = [Index(name = "idx_student_public_id", columnList = "public_id"), Index(name = "idx_student_email", columnList = "email")])
class Student(
    @ManyToOne(fetch = FetchType.LAZY)
    var person: Person? = null,
    @Column(nullable = false, unique = true)
    var studentId: String,
    @Column(nullable = false, unique = true)
    var email: String,
) : BaseEntity()
