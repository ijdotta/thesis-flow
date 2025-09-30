package ar.edu.uns.cs.thesisflow.people.mapper

import ar.edu.uns.cs.thesisflow.people.dto.PersonDTO
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import org.mapstruct.*
import java.util.UUID

@Mapper(componentModel = "spring")
interface PersonMapper {
    @Mapping(target = "publicId", source = "publicId", qualifiedByName = ["uuidToString"])
    fun toDto(entity: Person): PersonDTO

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    fun toEntity(dto: PersonDTO): Person

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "id", ignore = true)
    fun updateEntityFromDto(dto: PersonDTO, @MappingTarget entity: Person)

    @Named("uuidToString")
    fun uuidToString(uuid: UUID?): String? = uuid?.toString()
}
