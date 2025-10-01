package ar.edu.uns.cs.thesisflow.projects.mapper

import ar.edu.uns.cs.thesisflow.projects.dto.TagDTO
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.Tag
import org.mapstruct.*
import java.util.UUID

@Mapper(componentModel = "spring")
interface TagMapper {
    @Mapping(target = "publicId", source = "publicId", qualifiedByName = ["uuidToString"])
    fun toDto(entity: Tag): TagDTO

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    fun toEntity(dto: TagDTO): Tag

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    fun updateEntityFromDto(dto: TagDTO, @MappingTarget entity: Tag)

    @Named("uuidToString")
    fun uuidToString(uuid: UUID?): String? = uuid?.toString()
}
