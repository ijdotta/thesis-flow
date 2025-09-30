package ar.edu.uns.cs.thesisflow.projects.mapper

import ar.edu.uns.cs.thesisflow.projects.dto.ProjectDTO
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.Project
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectSubType
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectType
import org.mapstruct.*
import java.time.Instant
import java.util.UUID

@Mapper(
    componentModel = "spring",
    uses = [ApplicationDomainMapper::class, TagMapper::class]
)
interface ProjectMapper {

    @Mappings(
        Mapping(target = "publicId", source = "publicId", qualifiedByName = ["projectUuidToString"]),
        Mapping(target = "type", source = "type", qualifiedByName = ["enumToName"]),
        Mapping(target = "subtype", source = "subType", qualifiedByName = ["subtypeSetToList"]),
        Mapping(target = "applicationDomainDTO", source = "applicationDomain"),
        Mapping(target = "tags", source = "tags"),
        Mapping(target = "participants", ignore = true),
    )
    fun toDto(entity: Project): ProjectDTO

    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(target = "publicId", ignore = true),
        Mapping(target = "type", source = "type", qualifiedByName = ["nameToType"]),
        Mapping(target = "subType", source = "subtype", qualifiedByName = ["listToSubtypeSet"]),
        Mapping(target = "applicationDomain", ignore = true),
        Mapping(target = "tags", ignore = true),
        Mapping(target = "participants", ignore = true),
        Mapping(target = "createdAt", ignore = true),
        Mapping(target = "updatedAt", ignore = true),
    )
    fun toEntity(dto: ProjectDTO): Project

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings(
        Mapping(target = "id", ignore = true),
        Mapping(target = "publicId", ignore = true),
        Mapping(target = "applicationDomain", ignore = true),
        Mapping(target = "tags", ignore = true),
        Mapping(target = "participants", ignore = true),
        Mapping(target = "createdAt", ignore = true),
        Mapping(target = "updatedAt", ignore = true),
        Mapping(target = "type", source = "type", qualifiedByName = ["nameToType"]),
        Mapping(target = "subType", source = "subtype", qualifiedByName = ["listToSubtypeSet"]),
    )
    fun updateEntityFromDto(dto: ProjectDTO, @MappingTarget entity: Project)

    @AfterMapping
    fun afterToEntity(dto: ProjectDTO, @MappingTarget entity: Project) {
        dto.initialSubmission?.let { entity.initialSubmission = it }
        entity.completion = dto.completion
        entity.updatedAt = Instant.now()
    }

    @Named("projectUuidToString")
    fun projectUuidToString(uuid: UUID?): String? = uuid?.toString()

    @Named("enumToName")
    fun enumToName(e: Enum<*>?): String? = e?.name

    @Named("subtypeSetToList")
    fun subtypeSetToList(set: Set<ProjectSubType>?): List<String>? = set?.map { it.name }

    @Named("nameToType")
    fun nameToType(name: String?): ProjectType? = name?.let { ProjectType.valueOf(it) }

    @Named("listToSubtypeSet")
    fun listToSubtypeSet(list: List<String>?): MutableSet<ProjectSubType> =
        list?.map { ProjectSubType.valueOf(it) }?.toMutableSet() ?: mutableSetOf()
}
