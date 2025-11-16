package ar.edu.uns.cs.thesisflow.projects.persistance.repository

import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.Project
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectParticipant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying

interface ProjectParticipantRepository: JpaRepository<ProjectParticipant, Long> {
    fun findAllByProject(project: Project): List<ProjectParticipant>
    
    @Modifying
    fun deleteAllByProject(project: Project)
    
    fun findAllByPerson(person: Person): List<ProjectParticipant>
}