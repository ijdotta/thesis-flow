package ar.edu.uns.cs.thesisflow.people.mapper

import ar.edu.uns.cs.thesisflow.people.dto.ProfessorDTO
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Professor
import org.mapstruct.*
import java.util.UUID

@Mapper(componentModel = "spring")
interface ProfessorMapper {
    @Mappings(
        Mapping(target = "personPublicId", source = "person.publicId", qualifiedByName = ["uuidToString"]),
        Mapping(target = "name", source = "person.name"),
        Mapping(target = "lastname", source = "person.lastname"),
        Mapping(target = "publicId", source = "publicId", qualifiedByName = ["uuidToString"]),
    )
    fun toDto(entity: Professor): ProfessorDTO

    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(target = "publicId", ignore = true),
        Mapping(target = "person", source = "person"),
    )
    fun toEntity(dto: ProfessorDTO, person: Person): Professor

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings(
        Mapping(target = "person", ignore = true),
        Mapping(target = "id", ignore = true),
        Mapping(target = "publicId", ignore = true),
    )
    fun updateEntityFromDto(dto: ProfessorDTO, @MappingTarget entity: Professor)

    @Named("uuidToString")
    fun uuidToString(uuid: UUID?): String? = uuid?.toString()
}
