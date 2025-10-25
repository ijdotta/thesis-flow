package ar.edu.uns.cs.thesisflow.bulk

import ar.edu.uns.cs.thesisflow.catalog.persistance.entity.Career
import ar.edu.uns.cs.thesisflow.catalog.persistance.repository.CareerRepository
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Person
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Professor
import ar.edu.uns.cs.thesisflow.people.persistance.entity.Student
import ar.edu.uns.cs.thesisflow.people.persistance.entity.StudentCareer
import ar.edu.uns.cs.thesisflow.people.persistance.repository.PersonRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.ProfessorRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.StudentCareerRepository
import ar.edu.uns.cs.thesisflow.people.persistance.repository.StudentRepository
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ApplicationDomain
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ParticipantRole
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.Project
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectParticipant
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.ProjectType
import ar.edu.uns.cs.thesisflow.projects.persistance.entity.Tag
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ApplicationDomainRepository
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ProjectParticipantRepository
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.ProjectRepository
import ar.edu.uns.cs.thesisflow.projects.persistance.repository.TagRepository
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Service
class LegacyDatasetImporter(
    private val personRepository: PersonRepository,
    private val professorRepository: ProfessorRepository,
    private val studentRepository: StudentRepository,
    private val studentCareerRepository: StudentCareerRepository,
    private val careerRepository: CareerRepository,
    private val projectRepository: ProjectRepository,
    private val projectParticipantRepository: ProjectParticipantRepository,
    private val applicationDomainRepository: ApplicationDomainRepository,
    private val tagRepository: TagRepository,
) {

    private val logger = LoggerFactory.getLogger(LegacyDatasetImporter::class.java)
    private val dateFormatter = DateTimeFormatter.ofPattern("d/M/yyyy")
    private val typeMapping = mapOf(
        "PF" to ProjectType.FINAL_PROJECT,
        "TL" to ProjectType.THESIS,
    )
    private val nameNormalizerLocale = Locale.forLanguageTag("es-AR")

    private val personCache = mutableMapOf<NameKey, Person>()
    private val professorCache = mutableMapOf<NameKey, Professor>()
    private val studentCache = mutableMapOf<NameKey, Student>()
    private val tagCache = mutableMapOf<String, Tag>()
    private val domainCache = mutableMapOf<String, ApplicationDomain>()

    @Transactional
    fun import() {
        val resource = ClassPathResource("data/dataset.csv")
        if (!resource.exists()) {
            logger.warn("Legacy dataset file not found at data/dataset.csv - skipping import")
            return
        }

        resource.inputStream.use { input ->
            InputStreamReader(input, Charsets.UTF_8).use { reader ->
                val format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setTrim(true)
                    .setIgnoreEmptyLines(true)
                    .build()
                val parser = CSVParser.parse(reader, format)

                val defaultCareer = resolveDefaultCareer()
                parser.forEach { record ->
                    val title = record[TITLE]?.trim()?.takeIf { it.isNotBlank() } ?: return@forEach
                    val typeCode = record[PROJECT_TYPE]?.trim()?.uppercase(Locale.getDefault())
                    val projectType = typeMapping[typeCode]
                    if (projectType == null) {
                        logger.warn("Unknown project type '{}' for title '{}' - skipping", typeCode, title)
                        return@forEach
                    }

                    val initialSubmission = parseDate(record[INITIAL_SUBMISSION])
                    val completion = parseDate(record[COMPLETION])

                    if (initialSubmission == null) {
                        logger.warn("Initial submission missing for '{}' - skipping", title)
                        return@forEach
                    }

                    if (projectRepository.existsByTitleAndInitialSubmission(title, initialSubmission)) {
                        logger.debug("Project '{}' on {} already exists - skipping", title, initialSubmission)
                        return@forEach
                    }

                    val project = Project(
                        title = title,
                        type = projectType,
                        initialSubmission = initialSubmission,
                        completion = completion,
                    )
                    project.career = defaultCareer
                    project.applicationDomain = resolveApplicationDomain(record[APPLICATION_DOMAIN])
                    project.tags = resolveTags(record[TAGS]).toMutableSet()

                    val savedProject = projectRepository.save(project)

        val directorNames = parsePeopleField(record[DIRECTOR])
        val coDirectorNames = parsePeopleField(record[CO_DIRECTOR])
        val collaboratorsNames = parsePeopleField(record[COLLABORATOR])
        val studentNames = parsePeopleField(record[STUDENT_1]) +
            parsePeopleField(record[STUDENT_2]) +
            parsePeopleField(record[STUDENT_3])

                    val participants = mutableListOf<ProjectParticipant>()

                    directorNames.forEach { name ->
                        val professor = ensureProfessor(name)
                        val person = requireNotNull(professor.person) { "Professor without person" }
                        participants += ProjectParticipant(
                            project = savedProject,
                            person = person,
                            participantRole = ParticipantRole.DIRECTOR,
                        )
                    }

                    coDirectorNames.forEach { name ->
                        val professor = ensureProfessor(name)
                        val person = requireNotNull(professor.person) { "Professor without person" }
                        participants += ProjectParticipant(
                            project = savedProject,
                            person = person,
                            participantRole = ParticipantRole.CO_DIRECTOR,
                        )
                    }

                    collaboratorsNames.forEach { name ->
                        val person = ensurePersonEntity(name)
                        participants += ProjectParticipant(
                            project = savedProject,
                            person = person,
                            participantRole = ParticipantRole.COLLABORATOR,
                        )
                    }

                    studentNames.forEach { name ->
                        val student = ensureStudent(name, defaultCareer)
                        val person = requireNotNull(student.person) { "Student without person" }
                        participants += ProjectParticipant(
                            project = savedProject,
                            person = person,
                            participantRole = ParticipantRole.STUDENT,
                        )
                    }

                    if (participants.isNotEmpty()) {
                        val distinctParticipants = participants
                            .groupBy { participant ->
                                val person = requireNotNull(participant.person) { "Participant without person" }
                                participant.participantRole to person.publicId
                            }
                            .values
                            .map { it.first() }
                        projectParticipantRepository.saveAll(distinctParticipants)
                    }
                }
            }
        }
    }

    private fun resolveDefaultCareer(): Career {
        val name = "Legacy Dataset"
        return careerRepository.findByName(name) ?: careerRepository.save(Career(name = name))
    }

    private fun resolveApplicationDomain(raw: String?): ApplicationDomain? {
        val name = raw?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val normalized = normalizeLabel(name)
        return domainCache.getOrPut(normalized.lowercase(nameNormalizerLocale)) {
            applicationDomainRepository.findByName(normalized)
                ?: applicationDomainRepository.save(ApplicationDomain(name = normalized))
        }
    }

    private fun resolveTags(raw: String?): List<Tag> {
        val entries = splitCompositeField(raw, splitHyphen = false)
        return entries.mapNotNull { entry ->
            val normalized = normalizeLabel(entry)
            if (normalized.isBlank()) return@mapNotNull null
            tagCache.getOrPut(normalized.lowercase(nameNormalizerLocale)) {
                tagRepository.findByName(normalized)
                    ?: tagRepository.save(Tag(name = normalized))
            }
        }
    }

    private fun ensureProfessor(name: PersonName): Professor {
        val key = name.toKey()
        professorCache[key]?.let { return it }
        val person = ensurePersonEntity(name)
        val existing = professorRepository.findFirstByPerson(person)
        val professor = existing ?: professorRepository.save(
            Professor(
                person = person,
                email = generateProfessorEmail(name)
            )
        )
        professorCache[key] = professor
        return professor
    }

    private fun ensureStudent(name: PersonName, career: Career): Student {
        val key = name.toKey()
        studentCache[key]?.let { return it }
        val person = ensurePersonEntity(name)
        val existing = studentRepository.findFirstByPerson(person)
        val student = existing ?: studentRepository.save(
            Student(
                person = person,
                studentId = generateStudentId(name),
                email = generateStudentEmail(name)
            )
        )
        ensureStudentCareer(student, career)
        studentCache[key] = student
        return student
    }

    private fun ensureStudentCareer(student: Student, career: Career) {
        val existing = studentCareerRepository.findAllByStudent(student).firstOrNull { it.career?.id == career.id }
        if (existing == null) {
            studentCareerRepository.save(StudentCareer(student = student, career = career))
        }
    }

    private fun ensurePersonEntity(name: PersonName): Person {
        val key = name.toKey()
        personCache[key]?.let { return it }

        val normalizedName = normalizePersonComponent(name.first)
        val normalizedLastName = normalizePersonComponent(name.last)

        val existing = personRepository.findFirstByNameLikeIgnoreCaseAndLastnameLikeIgnoreCase(normalizedName, normalizedLastName)
        val person = existing ?: personRepository.save(Person(name = normalizedName, lastname = normalizedLastName))
        personCache[key] = person
        return person
    }

    private fun generateProfessorEmail(name: PersonName): String {
        val slug = name.slug()
        return "professor+$slug@cs.uns.edu.ar"
    }

    private fun generateStudentId(name: PersonName): String = "legacy-${name.slug()}"

    private fun generateStudentEmail(name: PersonName): String = "student+${name.slug()}@legacy.thesisflow"

    private fun parseDate(raw: String?): LocalDate? {
        val value = raw?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return runCatching { LocalDate.parse(value, dateFormatter) }
            .onFailure { logger.warn("Failed to parse date '{}'", value) }
            .getOrNull()
    }

    private fun parsePeopleField(raw: String?): List<PersonName> {
        return splitCompositeField(raw, splitHyphen = true).mapNotNull { parsePersonName(it) }
    }

    private fun parsePersonName(raw: String?): PersonName? {
        val value = raw?.trim()?.trim('"') ?: return null
        if (value.isBlank()) return null

        val sanitized = value.replace("\u00a0", " ") // non-breaking space
        return if (sanitized.contains(',')) {
            val parts = sanitized.split(',', limit = 2)
            val lastName = normalizePersonComponent(parts[0])
            val firstName = normalizePersonComponent(parts.getOrNull(1))
            PersonName(first = firstName, last = lastName)
        } else {
            val tokens = sanitized.split(Regex("\\s+"))
            val firstName = normalizePersonComponent(tokens.firstOrNull())
            val lastName = normalizePersonComponent(tokens.drop(1).joinToString(" ").ifBlank { tokens.firstOrNull() })
            PersonName(first = firstName, last = lastName)
        }
    }

    private fun normalizePersonComponent(raw: String?): String {
        val value = raw?.trim()?.takeIf { it.isNotBlank() } ?: "Unknown"
        return value.lowercase(nameNormalizerLocale)
            .split(Regex("\\s+"))
            .joinToString(" ") { word ->
                word.replaceFirstChar { ch ->
                    if (ch.isLowerCase()) ch.titlecase(nameNormalizerLocale) else ch.toString()
                }
            }
    }

    private fun normalizeLabel(raw: String): String {
        return raw.trim()
            .lowercase(nameNormalizerLocale)
            .split(Regex("\\s+"))
            .joinToString(" ") { word ->
                word.replaceFirstChar { ch ->
                    if (ch.isLowerCase()) ch.titlecase(nameNormalizerLocale) else ch.toString()
                }
            }
    }

    private fun splitCompositeField(raw: String?, splitHyphen: Boolean): List<String> {
        val value = raw?.trim()?.trim('"') ?: return emptyList()
        if (value.isBlank()) return emptyList()
        val normalized = value.replace(" y ", ";", ignoreCase = true)
        val regex = if (splitHyphen) {
            Regex("\\s*(?:-|/|;|,|\\|)\\s*")
        } else {
            Regex("\\s*(?:/|;|,|\\|)\\s*")
        }
        return normalized
            .split(regex)
            .mapNotNull { it.trim() }
            .filter { it.isNotBlank() }
    }

    private data class PersonName(val first: String, val last: String) {
        fun toKey(): NameKey = NameKey(first.lowercase(Locale.ROOT), last.lowercase(Locale.ROOT))
        fun slug(): String = ("$first-$last")
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .ifBlank { "unknown" }
    }

    private data class NameKey(val first: String, val last: String)

    companion object {
        private const val PROJECT_TYPE = "Tipo de proyecto"
        private const val INITIAL_SUBMISSION = "Fecha Consejo"
        private const val COMPLETION = "Fecha Finalizacion"
        private const val TITLE = "Titulo"
        private const val DIRECTOR = "Director"
        private const val CO_DIRECTOR = "Co-Director"
        private const val COLLABORATOR = "Colaborador"
        private const val STUDENT_1 = "Alumno 1"
        private const val STUDENT_2 = "Alumno 2"
        private const val STUDENT_3 = "Alumno 3"
        private const val TAGS = "Tema(s)"
        private const val APPLICATION_DOMAIN = "Area o Dominio de Aplicacion"
    }
}
