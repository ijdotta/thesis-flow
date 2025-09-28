package ar.edu.uns.cs.thesisflow.people.persistance.entity

import ar.edu.uns.cs.thesisflow.common.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(indexes = [Index(name = "idx_person_public_id", columnList = "public_id")])
class Person(
    @Column(nullable = false)
    var name: String = "",
    @Column(nullable = false)
    var lastname: String = "",
) : BaseEntity()
