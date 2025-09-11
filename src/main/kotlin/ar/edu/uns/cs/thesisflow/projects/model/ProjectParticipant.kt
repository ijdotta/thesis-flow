package ar.edu.uns.cs.thesisflow.projects.model

import ar.edu.uns.cs.thesisflow.people.model.Person
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class ProjectParticipant(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,
    @ManyToOne(fetch = FetchType.LAZY)
    var project: Project,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "person_public_id",
        referencedColumnName = "public_id",
        nullable = false,
        updatable = false,
    )
    var person: Person,
)