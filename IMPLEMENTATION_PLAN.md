# Filter Implementation Plan

Based on BE_FILTERING_REQUIREMENTS.md

## Endpoints to Update

### 1. GET /professors
**Current**: No filters  
**Required Filters**: `lastname`, `name`, `email`
**Implementation**: Similar to project filters - use JPA Specification

### 2. GET /students
**Current**: No filters  
**Required Filters**: `lastname`, `name`, `studentId`, `email`
**Implementation**: Similar to project filters

### 3. GET /people
**Current**: No filters  
**Required Filters**: `lastname`, `name`
**Implementation**: Similar structure

### 4. GET /careers
**Current**: No filters  
**Required Filters**: `name`
**Implementation**: Simple text filter

### 5. GET /application-domains
**Current**: No filters  
**Required Filters**: `name`, `description`
**Implementation**: Text filter for both fields

### 6. GET /tags
**Current**: No filters  
**Required Filters**: `name`, `description`
**Implementation**: Text filter for both fields

## Implementation Strategy

1. **Controller Layer**: Add @RequestParam for each filter
2. **Filter/Specification Layer**: Create similar structure to ProjectFilter & ProjectSpecifications
3. **Service Layer**: Update findAll() methods to use filters
4. **Tests**: Add test cases for each filter combination

## Filter Pattern to Follow (from projects)

```kotlin
// Filter Data Class
data class EntityFilter(
    val field1: String? = null,
    val field2: String? = null,
)

// JPA Specification
object EntitySpecifications {
    fun withFilter(filter: EntityFilter): Specification<Entity> {
        // Implementation using cb.like() for text fields
    }
}

// Controller
fun findAll(
    @RequestParam(required = false) field1: String?,
    @RequestParam(required = false) field2: String?,
    // ... page, size, sort
): ResponseEntity<*> {
    val filter = EntityFilter(field1, field2)
    val pageable = PageRequest.of(page, size, sort.toSort())
    return ResponseEntity.ok(service.findAll(pageable, filter))
}
```

## Order of Implementation

1. ProfessorController + ProfessorSpecifications
2. StudentController + StudentSpecifications
3. CareerController + CareerSpecifications
4. ApplicationDomainController + ApplicationDomainSpecifications
5. TagController + TagSpecifications
