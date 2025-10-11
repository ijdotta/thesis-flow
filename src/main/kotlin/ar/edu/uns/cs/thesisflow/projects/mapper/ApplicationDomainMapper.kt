package ar.edu.uns.cs.thesisflow.projects.mapper

import ar.edu.uns.cs.thesisflow.projects.dto.ApplicationDomainDTO
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ApplicationDomain
import org.mapstruct.*
import java.util.UUID

@Mapper(componentModel = "spring")
interface ApplicationDomainMapper {
    @Mapping(target = "publicId", source = "publicId", qualifiedByName = ["uuidToString"])
    fun toDto(entity: ApplicationDomain): ApplicationDomainDTO

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    fun toEntity(dto: ApplicationDomainDTO): ApplicationDomain

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    fun updateEntityFromDto(dto: ApplicationDomainDTO, @MappingTarget entity: ApplicationDomain)

    @Named("uuidToString")
    fun uuidToString(uuid: UUID?): String? = uuid?.toString()
}
