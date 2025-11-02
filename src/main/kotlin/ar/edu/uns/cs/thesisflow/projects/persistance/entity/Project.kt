package ar.edu.uns.cs.thesisflow.projects.persistance.entity

import ar.edu.uns.cs.thesisflow.catalog.persistance.entity.Career
import ar.edu.uns.cs.thesisflow.common.persistence.BaseEntity
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.EntityListeners
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.time.LocalDate

@Suppress("unused")
@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(indexes = [Index(name = "idx_project_public_id", columnList = "public_id")])
class Project(
    @Column(nullable = false)
    var title: String,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var type: ProjectType,
    @ElementCollection(targetClass = ProjectSubType::class, fetch = FetchType.LAZY)
    @CollectionTable(name = "project_subtypes", joinColumns = [JoinColumn(name = "project_id")])
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var subType: MutableSet<ProjectSubType> = mutableSetOf(),
    @Column(nullable = false)
    var initialSubmission: LocalDate = LocalDate.now(),
    var completion: LocalDate? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "career_id", nullable = false)
    var career: Career? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    var applicationDomain: ApplicationDomain? = null,
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "project_tags",
        joinColumns = [JoinColumn(name = "project_id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id")]
    )
    var tags: MutableSet<Tag> = mutableSetOf(),
    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
    var participants: MutableSet<ProjectParticipant> = mutableSetOf(),
    @Column(nullable = false)
    var resources: String = "[]",
) : BaseEntity() {
    @CreatedDate
    @Column(nullable = false, updatable = false)
    lateinit var createdAt: Instant
    @LastModifiedDate
    @Column(nullable = false)
    lateinit var updatedAt: Instant
}
