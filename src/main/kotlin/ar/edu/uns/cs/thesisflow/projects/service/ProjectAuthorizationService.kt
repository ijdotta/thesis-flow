package ar.edu.uns.cs.thesisflow.projects.service

import ar.edu.uns.cs.thesisflow.auth.model.UserRole
import ar.edu.uns.cs.thesisflow.auth.service.CurrentUserService
import ar.edu.uns.cs.thesisflow.people.persistance.repository.ProfessorRepository
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ParticipantRole
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.Project
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ProjectParticipantRepository
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service

@Service
class ProjectAuthorizationService(
    private val currentUserService: CurrentUserService,
    private val professorRepository: ProfessorRepository,
    private val projectParticipantRepository: ProjectParticipantRepository,
) {

    fun ensureCanModify(project: Project) {
        val user = currentUserService.requireCurrentUser()
        when (user.role) {
            UserRole.ADMIN -> return
            UserRole.PROFESSOR -> ensureProfessorOwnsProject(project)
        }
    }

    private fun ensureProfessorOwnsProject(project: Project) {
        val professorPublicId = currentUserService.professorPublicId()
            ?: throw AccessDeniedException("Professor account is not linked to a professor profile")
        val professor = professorRepository.findByPublicId(professorPublicId)
            ?: throw AccessDeniedException("Linked professor not found")
        val person = professor.person
            ?: throw AccessDeniedException("Linked professor has no associated person")

        val hasAccess = projectParticipantRepository.findAllByProject(project)
            .any { participant ->
                participant.person.id == person.id &&
                    participant.participantRole in setOf(ParticipantRole.DIRECTOR, ParticipantRole.CO_DIRECTOR)
            }

        if (!hasAccess) {
            throw AccessDeniedException("Professor cannot modify a project they do not own")
        }
    }
}
