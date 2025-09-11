package ar.edu.uns.cs.thesisflow.people.model

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.util.UUID

@Entity
class Person(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,
    var publicId: UUID = UUID.randomUUID(),
    var name: String,
    var lastname: String,
)