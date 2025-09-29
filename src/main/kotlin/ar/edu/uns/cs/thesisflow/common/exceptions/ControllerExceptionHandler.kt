package ar.edu.uns.cs.thesisflow.common.exceptions

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.time.Instant

@RestControllerAdvice
class ControllerExceptionHandler {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.info("Type mismatch for parameter '${ex.name}': ${ex.value}", ex)
        return errorResponse(HttpStatus.BAD_REQUEST, ex, request)
    }

    @ExceptionHandler(ValidationException::class)
    fun handleValidation(ex: ValidationException, request: HttpServletRequest) = errorResponse(HttpStatus.BAD_REQUEST, ex, request)

    @ExceptionHandler(ConflictException::class)
    fun handleConflict(ex: ConflictException, request: HttpServletRequest) = errorResponse(HttpStatus.CONFLICT, ex, request)

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException, request: HttpServletRequest) = errorResponse(HttpStatus.NOT_FOUND, ex, request)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArg(ex: IllegalArgumentException, request: HttpServletRequest) = errorResponse(HttpStatus.BAD_REQUEST, ex, request)

    @ExceptionHandler(Exception::class)
    fun handleUnhandled(ex: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        logger.error(ex.message, ex)
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex, request)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(ex: MethodArgumentNotValidException, request: HttpServletRequest): ResponseEntity<ValidationErrorResponse> {
        val fieldErrors = ex.bindingResult.fieldErrors.map { FieldValidationError(it.field, it.defaultMessage ?: "invalid") }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ValidationErrorResponse(
                message = "Validation failed",
                timestamp = Instant.now().toString(),
                path = request.requestURL.toString(),
                errors = fieldErrors
            )
        )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException, request: HttpServletRequest): ResponseEntity<ValidationErrorResponse> {
        val violations = ex.constraintViolations.map { FieldValidationError(it.propertyPath.toString(), it.message) }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ValidationErrorResponse(
                message = "Validation failed",
                timestamp = Instant.now().toString(),
                path = request.requestURL.toString(),
                errors = violations
            )
        )
    }

    private fun errorResponse(status: HttpStatus, ex: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        if (status.is5xxServerError) {
            logger.error(ex.message, ex)
        } else {
            logger.info(ex.message ?: ex.javaClass.simpleName)
        }
        return ResponseEntity.status(status).body(getErrorResponse(ex, request))
    }

    private fun getErrorResponse(ex: Exception, request: HttpServletRequest) = ErrorResponse(
        message = ex.localizedMessage ?: "Unexpected error",
        timestamp = Instant.now().toString(),
        path = request.requestURL.toString()
    )
}

data class ErrorResponse(
    val message: String,
    val timestamp: String,
    val path: String,
)

data class ValidationErrorResponse(
    val message: String,
    val timestamp: String,
    val path: String,
    val errors: List<FieldValidationError>
)

data class FieldValidationError(
    val field: String,
    val error: String,
)
