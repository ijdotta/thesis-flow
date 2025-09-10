package ar.edu.uns.cs.thesisflow.catalog.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(indexes = [Index(name = "uuid", columnList = "uuid")])
class Career(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,
    @Column(nullable = false, unique = true, updatable = false)
    var uuid: UUID = UUID.randomUUID(),
    @Column(nullable = false, unique = true)
    var name: String,
)