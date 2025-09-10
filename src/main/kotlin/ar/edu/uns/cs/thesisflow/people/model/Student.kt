package ar.edu.uns.cs.thesisflow.people.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.util.UUID

@Entity
class Student(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,
    @Column(nullable = false, unique = true, updatable = false)
    var uuid: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    var name: String,
    @Column(nullable = false)
    var lastname: String,
    @Column(nullable = false, unique = true)
    var studentId: String,
    @Column(nullable = false, unique = true)
    var email: String,
    @Column(nullable = false)
    var careerId: String,
)