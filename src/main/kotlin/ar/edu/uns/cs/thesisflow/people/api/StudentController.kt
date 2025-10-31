package ar.edu.uns.cs.thesisflow.people.api

import ar.edu.uns.cs.thesisflow.people.dto.StudentDTO
import ar.edu.uns.cs.thesisflow.people.service.StudentService
import ar.edu.uns.cs.thesisflow.people.service.StudentFilter
import ar.edu.uns.cs.thesisflow.people.service.StudentSpecifications
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/students")
class StudentController(
    private val studentService: StudentService,
) {

    @GetMapping
    fun findAll(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "25") size: Int,
        @RequestParam(required = false) lastname: String?,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) studentId: String?,
        @RequestParam(required = false) email: String?,
        @RequestParam(required = false) sort: String?,
    ): ResponseEntity<*> {
        val filter = StudentFilter(
            lastname = lastname?.takeIf { it.isNotBlank() },
            name = name?.takeIf { it.isNotBlank() },
            studentId = studentId?.takeIf { it.isNotBlank() },
            email = email?.takeIf { it.isNotBlank() },
        )
        val pageable = PageRequest.of(page, size)
        return ResponseEntity.ok(studentService.findAll(pageable, filter, StudentSpecifications.withFilter(filter)))
    }

    @GetMapping("/{publicId}")
    fun findByPublicId(@PathVariable publicId: String) = ResponseEntity
        .ok(studentService.findByPublicId(publicId))

    @PostMapping
    fun create(@RequestBody request: StudentDTO) = ResponseEntity
        .ok(studentService.create(request))

    @PutMapping("/{publicId}")
    fun update(@PathVariable publicId: String, @RequestBody request: StudentDTO) = with (request) {
        val studentWithId = copy(publicId)
        ResponseEntity.ok(studentService.update(studentWithId))
    }

    @PutMapping("/{publicId}/careers")
    fun updateCareers(@PathVariable publicId: String, @RequestBody updateCareersRequest: UpdateCareersRequest) = ResponseEntity
        .ok(studentService.updateCareers(publicId, updateCareersRequest.careers))
}

