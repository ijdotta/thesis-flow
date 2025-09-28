package ar.edu.uns.cs.thesisflow.catalog.persistance.entity

import ar.edu.uns.cs.thesisflow.common.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(indexes = [Index(name = "idx_career_public_id", columnList = "public_id")])
class Career(
    @Column(nullable = false, unique = true)
    var name: String,
) : BaseEntity()
