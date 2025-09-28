package ar.edu.uns.cs.thesisflow.people.persistance.entity

import ar.edu.uns.cs.thesisflow.common.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(indexes = [Index(name = "idx_professor_public_id", columnList = "public_id"), Index(name = "idx_professor_email", columnList = "email")])
class Professor(
    @ManyToOne(fetch = FetchType.LAZY)
    var person: Person,
    @Column(nullable = false, unique = true)
    var email: String,
) : BaseEntity()
