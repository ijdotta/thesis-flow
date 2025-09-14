package ar.edu.uns.cs.thesisflow.people.api

import ar.edu.uns.cs.thesisflow.people.dto.StudentDTO
import ar.edu.uns.cs.thesisflow.people.service.StudentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/students")
class StudentController(
    private val studentService: StudentService,
) {

    @GetMapping
    fun findAll() = ResponseEntity
        .ok(studentService.findAll())

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
}