package ar.edu.uns.cs.thesisflow.projects.api

import ar.edu.uns.cs.thesisflow.projects.dto.TagDTO
import ar.edu.uns.cs.thesisflow.projects.service.TagService
import ar.edu.uns.cs.thesisflow.projects.service.TagFilter
import ar.edu.uns.cs.thesisflow.projects.service.TagSpecifications
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.DeleteMapping

@RestController
@RequestMapping("/tags")
class TagController(
    val tagService: TagService
) {
    @GetMapping
    fun findAll(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "25") size: Int,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) description: String?,
        @RequestParam(required = false) sort: String?,
    ) = ResponseEntity.ok(tagService.findAll(
        PageRequest.of(page, size),
        TagFilter(
            name = name?.takeIf { it.isNotBlank() },
            description = description?.takeIf { it.isNotBlank() }
        ),
        TagSpecifications.withFilter(
            TagFilter(
                name = name?.takeIf { it.isNotBlank() },
                description = description?.takeIf { it.isNotBlank() }
            )
        )
    ))

    @GetMapping("/{publicId}")
    fun findByPublicId(@PathVariable publicId: String) =
        ResponseEntity.ok(tagService.findByPublicId(publicId))

    @PostMapping
    fun create(@RequestBody tag: TagDTO) = ResponseEntity.ok(tagService.create(tag))

    @PutMapping("/{publicId}")
    fun update(@PathVariable publicId: String, @RequestBody tag: TagDTO) =
        ResponseEntity.ok(tagService.update(publicId, tag))

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: String) = tagService.delete(id)
}