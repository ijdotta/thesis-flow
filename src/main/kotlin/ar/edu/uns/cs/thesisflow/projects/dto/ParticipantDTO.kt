package ar.edu.uns.cs.thesisflow.projects.dto

import ar.edu.uns.cs.thesisflow.people.dto.PersonDTO
import ar.edu.uns.cs.thesisflow.people.dto.toDTO
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectParticipant

data class ParticipantDTO(
    val personDTO: PersonDTO,
    val role: String,
)

fun ProjectParticipant.toDTO() = ParticipantDTO(
    personDTO = person.toDTO(),
    role = participantRole.name,
)
