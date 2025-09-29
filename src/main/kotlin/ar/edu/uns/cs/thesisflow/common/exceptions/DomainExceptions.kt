package ar.edu.uns.cs.thesisflow.common.exceptions

open class DomainException(message: String): RuntimeException(message)
class NotFoundException(message: String): DomainException(message)
class ValidationException(message: String): DomainException(message)
class ConflictException(message: String): DomainException(message)

