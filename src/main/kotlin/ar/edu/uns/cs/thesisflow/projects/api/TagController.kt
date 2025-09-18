package ar.edu.uns.cs.thesisflow.projects.api

import ar.edu.uns.cs.thesisflow.projects.dto.TagDTO
import ar.edu.uns.cs.thesisflow.projects.service.TagService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/tags")
class TagController(
    val tagService: TagService
) {
    @GetMapping
    fun findAll() = ResponseEntity.ok(tagService.findAll())

    @GetMapping("/{publicId}")
    fun findByPublicId(@PathVariable publicId: String) =
        ResponseEntity.ok(tagService.findByPublicId(publicId))

    @PostMapping
    fun create(@RequestBody tag: TagDTO) = ResponseEntity.ok(tagService.create(tag))

    @PutMapping("/{publicId}")
    fun update(@PathVariable publicId: String, @RequestBody tag: TagDTO) =
        ResponseEntity.ok(tagService.update(publicId, tag))
}