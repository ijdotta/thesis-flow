package ar.edu.uns.cs.thesisflow.people.persistance.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(indexes = [Index(name = "public_id", columnList = "public_id")])
class Person(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,
    @Column(name = "public_id", nullable = false, unique = true)
    var publicId: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    var name: String,
    @Column(nullable = false)
    var lastname: String,
)