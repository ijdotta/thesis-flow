package ar.edu.uns.cs.thesisflow.projects.dto

import ar.edu.uns.cs.thesisflow.projects.persistance.entity.Tag

data class TagDTO(
    var publicId: String? = null,
    var name: String? = null,
    var description: String? = null,
) {
    fun toEntity() = Tag(name = name!!, description = description)
    fun update(entity: Tag) {
        name?.let { entity.name = it }
        description?.let { entity.description = it }
    }
}

fun Tag.toDTO() = TagDTO(
    publicId = publicId.toString(),
    name = name,
    description = description,
)
