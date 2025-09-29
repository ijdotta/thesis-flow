package ar.edu.uns.cs.thesisflow.projects.persistance.entity

import ar.edu.uns.cs.thesisflow.common.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    indexes = [Index(name = "idx_tag_public_id", columnList = "public_id"), Index(name = "idx_tag_name", columnList = "name")]
)
class Tag(
    @Column(unique = true, nullable = false)
    var name: String,
    @Column(nullable = true)
    var description: String? = null,
) : BaseEntity()
