package ar.edu.uns.cs.thesisflow.people.mapper

import ar.edu.uns.cs.thesisflow.people.dto.StudentDTO
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Student
import org.mapstruct.*
import java.util.UUID

@Mapper(componentModel = "spring")
interface StudentMapper {
    @Mappings(
        Mapping(target = "personPublicId", source = "person.publicId"),
        Mapping(target = "name", source = "person.name"),
        Mapping(target = "lastname", source = "person.lastname"),
        Mapping(target = "publicId", source = "publicId", qualifiedByName = ["uuidToString"]),
        Mapping(target = "careers", expression = "java(java.util.Collections.emptyList())"),
    )
    fun toDto(entity: Student): StudentDTO

    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(target = "publicId", ignore = true),
        Mapping(target = "person", source = "person"),
    )
    fun toEntity(dto: StudentDTO, person: Person): Student

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(target = "publicId", ignore = true),
        Mapping(target = "person", ignore = true),
        Mapping(target = "personPublicId", ignore = true),
        Mapping(target = "name", ignore = true),
        Mapping(target = "lastname", ignore = true),
        Mapping(target = "careers", ignore = true),
    )
    fun updateEntityFromDto(dto: StudentDTO, @MappingTarget entity: Student)

    @Named("uuidToString")
    fun uuidToString(uuid: UUID?): String? = uuid?.toString()
}
