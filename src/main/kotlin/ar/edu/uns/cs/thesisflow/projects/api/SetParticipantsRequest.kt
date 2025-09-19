package ar.edu.uns.cs.thesisflow.projects.api

import ar.edu.uns.cs.thesisflow.projects.dto.ParticipantInfo

data class SetParticipantsRequest(
    val participants: List<ParticipantInfo>,
)

