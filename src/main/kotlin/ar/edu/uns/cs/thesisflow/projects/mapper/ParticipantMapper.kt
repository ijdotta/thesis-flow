package ar.edu.uns.cs.thesisflow.projects.mapper

import ar.edu.uns.cs.thesisflow.people.mapper.PersonMapper
import ar.edu.uns.cs.thesisflow.projects.dto.ParticipantDTO
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectParticipant
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings

@Mapper(componentModel = "spring", uses = [PersonMapper::class])
interface ParticipantMapper {
    @Mappings(
        Mapping(target = "personDTO", source = "person"),
        Mapping(target = "role", source = "participantRole")
    )
    fun toDto(entity: ProjectParticipant): ParticipantDTO
}

