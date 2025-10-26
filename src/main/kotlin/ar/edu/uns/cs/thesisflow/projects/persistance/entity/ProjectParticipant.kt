package ar.edu.uns.cs.thesisflow.projects.persistance.entity

import ar.edu.uns.cs.thesisflow.common.persistence.BaseEntity
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    indexes = [Index(name = "idx_project_participant_public_id", columnList = "public_id")],
    uniqueConstraints = [UniqueConstraint(name = "uc_project_person_role", columnNames = ["project_id", "person_id", "participant_role"])]
)
class ProjectParticipant(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false, updatable = false)
    var project: Project,
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "person_id", nullable = false, updatable = false)
    var person: Person,
    @Enumerated(EnumType.STRING)
    @Column(name = "participant_role", nullable = false)
    var participantRole: ParticipantRole
) : BaseEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProjectParticipant) return false
        return publicId == other.publicId
    }
    override fun hashCode(): Int = publicId.hashCode()
}
