package ar.edu.uns.cs.thesisflow.projects.bulk

import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Student
import ar.edu.uns.cs.thesisflow.people.persistance.repository.PersonRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.StudentRepository
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ApplicationDomain
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.Project
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.Tag
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ApplicationDomainRepository
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.TagRepository
import java.time.LocalDate

class RawProjectsHandlerImpl(
    private val applicationDomainRepository: ApplicationDomainRepository,
    private val tagRepository: TagRepository,
    private val personRepository: PersonRepository,
    private val studentRepository: StudentRepository,
) : RawProjectsHandler {
    override fun insertRawProject(rawProjectData: RawProjectData) {
        with(rawProjectData) {
            val tags = topics.asStringTags().findOrCreateTags()
            val studentA = studentA.findOrCreateStudent()
            val studentB = studentB?.findOrCreateStudent()

            Project(
                title = title.normalized(),
                type = type.asProjectType(),
//                subType = null,
                initialSubmission = submissionDate.asDate() ?: LocalDate.now(),
                completion = completionDate.asDate(),
//                career = TODO(),
                applicationDomain = domain.findOrCreateDomain(),
//                tags = TODO(),
//                participants = TODO(),
            )
        }
    }

    private fun String.findOrCreateDomain(): ApplicationDomain {
        val normalized = this.normalized()
        return with(applicationDomainRepository) {
            findByName(normalized) ?: save(ApplicationDomain(name = normalized))
        }
    }

    private fun Set<String>.findOrCreateTags(): List<Tag> {
        return this.map { tagName ->
            with(tagRepository) {
                findByName(tagName) ?: save(Tag(name = tagName))
            }
        }.toList()
    }

    private fun String.findOrCreateStudent(): Student {
        val person = findOrCreatePerson()
        return studentRepository.findFirstByPerson(person) ?: studentRepository.save(Student(
            person = person,
            studentId = "UNKNOWN ID",
            email = "UNKNOWN EMAIL",
        ))
    }

    private fun String.findOrCreatePerson(): Person {
        val split = split(",")
        val lastName = split.getOrNull(0)?.normalized() ?: ""
        val firstName = split.getOrNull(1)?.normalized() ?: ""
        return with(personRepository) {
            findFirstByNameLikeIgnoreCaseAndLastnameLikeIgnoreCase(firstName, lastName)
                ?: save(Person(name = firstName, lastname = lastName))
        }
    }
}