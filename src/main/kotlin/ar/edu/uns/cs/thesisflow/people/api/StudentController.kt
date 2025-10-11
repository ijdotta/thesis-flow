package ar.edu.uns.cs.thesisflow.people.api

import ar.edu.uns.cs.thesisflow.people.dto.StudentDTO
import ar.edu.uns.cs.thesisflow.people.service.StudentService
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ar.edu.uns.cs.thesisflow.common.ApiPaths
import ar.edu.uns.cs.thesisflow.common.PaginationDefaults

@RestController
@RequestMapping(ApiPaths.STUDENTS)
class StudentController(
    private val studentService: StudentService,
) {

    @GetMapping
    fun findAll(
        @RequestParam(required = false, defaultValue = PaginationDefaults.PAGE_STRING) page: Int,
        @RequestParam(required = false, defaultValue = PaginationDefaults.SIZE_STRING) size: Int,
    ) = ResponseEntity
        .ok(studentService.findAll(PageRequest.of(page, size)))

    @GetMapping("/{publicId}")
    fun findByPublicId(@PathVariable publicId: String) = ResponseEntity
        .ok(studentService.findByPublicId(publicId))

    @PostMapping
    fun create(@RequestBody request: StudentDTO) = ResponseEntity
        .ok(studentService.create(request))

    @PutMapping("/{publicId}")
    fun update(@PathVariable publicId: String, @RequestBody request: StudentDTO) = with (request) {
        val studentWithId = copy(publicId = publicId)
        ResponseEntity.ok(studentService.update(studentWithId))
    }

    @PutMapping("/{publicId}/careers")
    fun updateCareers(@PathVariable publicId: String, @RequestBody careerIds: List<String>) = ResponseEntity
        .ok(studentService.updateCareers(publicId, careerIds))
}