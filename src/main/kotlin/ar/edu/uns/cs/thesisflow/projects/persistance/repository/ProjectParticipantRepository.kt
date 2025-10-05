package ar.edu.uns.cs.thesisflow.projects.persistance.repository

import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.Project
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectParticipant
import org.springframework.data.jpa.repository.JpaRepository

interface ProjectParticipantRepository: JpaRepository<ProjectParticipant, Long> {
    fun findAllByProject(project: Project): List<ProjectParticipant>
    fun deleteAllByProject(project: Project)
    fun findAllByPerson(person: Person): List<ProjectParticipant>
}