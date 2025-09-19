package ar.edu.uns.cs.thesisflow.projects.persistance.entity

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(indexes = [Index(name = "public_id", columnList = "public_id")])
class Project(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "public_id", nullable = false, unique = true, updatable = false)
    var publicId: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    var title: String,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var type: ProjectType,
    @ElementCollection(targetClass = ProjectSubType::class, fetch = FetchType.EAGER)
    @CollectionTable(name = "project_subtypes", joinColumns = [JoinColumn(name = "project_id")])
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var subType: MutableSet<ProjectSubType> = mutableSetOf(),
    @Column(nullable = false)
    var initialSubmission: Instant = Instant.now(),
    @Column(nullable = true)
    var completion: Instant? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    var applicationDomain: ApplicationDomain? = null,
    @ManyToMany(fetch = FetchType.LAZY)
    var tags: MutableSet<Tag> = mutableSetOf(),
    @Column(nullable = false)
    var createdAt: Instant = Instant.now(),
    @Column(nullable = false)
    var updatedAt: Instant? = Instant.now(),
)