package ar.edu.uns.cs.thesisflow.people.persistance.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(indexes = [Index(name = "public_id", columnList = "public_id")])
class Professor(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(name = "public_id", nullable = false, unique = true, updatable = false)
    var publicId: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY)
    var person: Person,
    @Column(nullable = false, unique = true)
    var email: String,
)