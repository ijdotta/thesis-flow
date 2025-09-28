package ar.edu.uns.cs.thesisflow.projects.persistance.entity

import ar.edu.uns.cs.thesisflow.common.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(indexes = [Index(name = "idx_app_domain_public_id", columnList = "public_id")])
class ApplicationDomain(
    @Column(nullable = false, unique = true)
    var name: String,
    @Column(nullable = true)
    var description: String? = null,
) : BaseEntity()
