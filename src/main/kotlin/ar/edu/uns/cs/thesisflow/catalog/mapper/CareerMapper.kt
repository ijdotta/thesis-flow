package ar.edu.uns.cs.thesisflow.catalog.mapper

import ar.edu.uns.cs.thesisflow.catalog.dto.CareerDTO
import ar.edu.uns.cs.thesisflow.catalog.persistance.entity.Career
import org.mapstruct.*
import java.util.UUID

@Mapper(componentModel = "spring")
interface CareerMapper {
    @Mapping(target = "publicId", source = "publicId", qualifiedByName = ["uuidToString"])
    fun toDto(entity: Career): CareerDTO

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    fun toEntity(dto: CareerDTO): Career

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "id", ignore = true)
    fun updateEntityFromDto(dto: CareerDTO, @MappingTarget entity: Career)

    @Named("uuidToString")
    fun uuidToString(uuid: UUID?): String? = uuid?.toString()
}
