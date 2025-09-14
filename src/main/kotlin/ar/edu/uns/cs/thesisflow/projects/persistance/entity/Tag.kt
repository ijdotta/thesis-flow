package ar.edu.uns.cs.thesisflow.projects.persistance.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(indexes = [Index(name = "public_id", columnList = "public_id")])
class Tag(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,
    @Column(unique = true, nullable = false, updatable = false)
    var name: String,
    @Column(nullable = false)
    var description: String,
)