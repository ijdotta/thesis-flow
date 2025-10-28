# Backend API Specification: Browse Projects Filters Enhancement

**Date:** 2025-10-28  
**Scope:** Backend API updates required to support advanced filtering in the "Browse Projects" public view  
**Frontend PR Reference:** #20

---

## Overview

The frontend has implemented an advanced filter UI for the "Browse Projects" page. This specification details the required backend API changes to support these new filtering capabilities.

**New Filters:**
1. **Project Type** (Thesis / Final Project)
2. **Application Domain** (domain-based categorization)
3. **Professor** (Director or Co-director)
4. **Date Range** (from year to year)

---

## 1. Data Transfer Objects (DTOs)

### 1.1 Update `FilterMetadata` Response

**Current structure** (from `BE_API_REFERENCE.md`):
```
FiltersResponse: { careers: FilterOption[], professors: FilterOption[], yearRange: YearRange }
```

**New structure** (renamed and enhanced):

```kotlin
FilterMetadata {
  careers: FilterOption[]              // Existing
  professors: FilterOption[]            // Existing (directors & co-directors)
  applicationDomains: FilterOption[]    // NEW: All application domains
  yearRange: YearRange                  // Existing
}

// Supporting types
FilterOption {
  id: String (UUID)        // publicId
  name: String
}

YearRange {
  minYear: Int            // Minimum year from all projects' initialSubmission
  maxYear: Int            // Maximum year from all projects' initialSubmission
}
```

**Notes:**
- Rename `FiltersResponse` to `FilterMetadata` to match frontend expectations.
- Add `applicationDomains` array containing all available application domains with their public IDs.
- The professors list should include both DIRECTOR and CO_DIRECTOR roles.

---

## 2. Endpoint Specification

### 2.1 GET `/analytics/filters` (UPDATED)

**Purpose:** Fetch cached filter metadata for the browse projects page.

**Auth:** Public (no authentication required)

**Query Parameters:** None

**Response:** `200 OK`

```json
{
  "careers": [
    { "id": "550e8400-e29b-41d4-a716-446655440001", "name": "Licenciatura en Ciencias de la Computación" },
    { "id": "550e8400-e29b-41d4-a716-446655440002", "name": "Ingeniería en Sistemas" }
  ],
  "professors": [
    { "id": "550e8400-e29b-41d4-a716-446655440101", "name": "Dr. Juan Pérez" },
    { "id": "550e8400-e29b-41d4-a716-446655440102", "name": "Dra. María García" }
  ],
  "applicationDomains": [
    { "id": "550e8400-e29b-41d4-a716-446655440201", "name": "Machine Learning" },
    { "id": "550e8400-e29b-41d4-a716-446655440202", "name": "Web Development" },
    { "id": "550e8400-e29b-41d4-a716-446655440203", "name": "Distributed Systems" }
  ],
  "yearRange": {
    "minYear": 2015,
    "maxYear": 2025
  }
}
```

**Error Responses:**
- `500 Internal Server Error` if cache/database unavailable.

---

### 2.2 GET `/projects/public` (ENHANCED)

**Purpose:** Public-facing project listing with advanced filters.

**Auth:** Public (no authentication required)

**Query Parameters:**

| Name | Type | Required | Notes |
| --- | --- | --- | --- |
| `page` | int | No | Page number (0-based). Defaults to `0`. |
| `size` | int | No | Page size. Defaults to `20`. |
| `search` | string | No | Text search across project title and tag names (case-insensitive). |
| `projectTypes` | string | No | Comma-separated project types: `THESIS,FINAL_PROJECT`. Example: `THESIS` or `THESIS,FINAL_PROJECT`. |
| `applicationDomainIds` | string | No | Comma-separated application domain UUIDs. Invalid UUIDs are ignored. Example: `550e8400-e29b-41d4-a716-446655440201` |
| `professorIds` | string | No | Comma-separated professor UUIDs (matches DIRECTOR or CO_DIRECTOR roles). Invalid UUIDs are ignored. Example: `550e8400-e29b-41d4-a716-446655440101,550e8400-e29b-41d4-a716-446655440102` |
| `careerIds` | string | No | Comma-separated career UUIDs. Invalid UUIDs are ignored. (Existing filter) |
| `fromYear` | int | No | Inclusive lower bound on project `initialSubmission` year. |
| `toYear` | int | No | Inclusive upper bound on project `initialSubmission` year. |

**Request Example:**

```http
GET /projects/public?page=0&size=12&projectTypes=THESIS&applicationDomainIds=550e8400-e29b-41d4-a716-446655440201&professorIds=550e8400-e29b-41d4-a716-446655440101&fromYear=2020&toYear=2025&search=machine%20learning
```

**Response:** `200 OK`

```json
{
  "content": [
    {
      "publicId": "550e8400-e29b-41d4-a716-446655440301",
      "title": "Machine Learning for Climate Prediction",
      "type": "THESIS",
      "subtype": [],
      "initialSubmission": "2023-05-15",
      "completion": "2024-03-10",
      "career": {
        "publicId": "550e8400-e29b-41d4-a716-446655440001",
        "name": "Licenciatura en Ciencias de la Computación"
      },
      "applicationDomainDTO": {
        "publicId": "550e8400-e29b-41d4-a716-446655440201",
        "name": "Machine Learning"
      },
      "tags": [
        { "publicId": "550e8400-e29b-41d4-a716-446655440401", "name": "AI" },
        { "publicId": "550e8400-e29b-41d4-a716-446655440402", "name": "Climate" }
      ],
      "participants": [
        {
          "role": "DIRECTOR",
          "personDTO": {
            "publicId": "550e8400-e29b-41d4-a716-446655440101",
            "name": "Juan",
            "lastname": "Pérez"
          }
        },
        {
          "role": "STUDENT",
          "personDTO": {
            "publicId": "550e8400-e29b-41d4-a716-446655440501",
            "name": "Carlos",
            "lastname": "López"
          }
        }
      ]
    }
  ],
  "totalElements": 42,
  "totalPages": 4,
  "currentPage": 0,
  "size": 12
}
```

**Filtering Logic:**

1. **projectTypes Filter:**
   - If provided, only include projects whose `type` is in the list.
   - Case-sensitive matching against `THESIS` or `FINAL_PROJECT`.
   - Multiple types: combine with OR logic.

2. **applicationDomainIds Filter:**
   - If provided, only include projects whose `applicationDomainDTO.publicId` matches one of the IDs.
   - Invalid UUIDs are silently ignored (use `runCatching` pattern as per existing code).
   - Multiple IDs: combine with OR logic.

3. **professorIds Filter:**
   - If provided, only include projects that have a participant matching:
     - `participant.role` is `DIRECTOR` OR `CO_DIRECTOR`, AND
     - `participant.personDTO.publicId` is in the provided IDs.
   - Invalid UUIDs are silently ignored.
   - Multiple IDs: combine with OR logic.

4. **Date Range Filter (fromYear / toYear):**
   - Extract the year from each project's `initialSubmission` date.
   - If `fromYear` is provided: only include projects where `year >= fromYear`.
   - If `toYear` is provided: only include projects where `year <= toYear`.
   - Both can be used together (inclusive range).

5. **Existing Filters (careerIds, search):**
   - Continue to apply as documented in `BE_API_REFERENCE.md`.
   - Combine all filters with AND logic (only projects matching ALL criteria are returned).

**Error Responses:**
- `200 OK` (with empty content) if no projects match.
- `400 Bad Request` if query parameters are malformed (e.g., invalid `page` or `size` types).

---

## 3. Implementation Guidelines

### 3.1 Filter Application

Add filter parameters to the existing `ProjectRepository` query or `ProjectService.browseProjects()` method.

**Pseudocode for filter combination:**

```
filters = [
  careerIds (existing),
  search (existing),
  projectTypes (new),
  applicationDomainIds (new),
  professorIds (new),
  fromYear (existing, may need enhancement),
  toYear (existing, may need enhancement)
]

return projects where (all filters match)
```

### 3.2 Cache Management for `/analytics/filters`

- The `FilterMetadata` endpoint should return:
  - All unique careers from projects.
  - All unique professors (those with DIRECTOR or CO_DIRECTOR roles in any project).
  - **All application domains** (from the ApplicationDomain catalog, not filtered by project association).
  - Min/max years computed from all projects' `initialSubmission`.
- Consider caching this response (e.g., 1-hour TTL) as it's public and computationally stable.

### 3.3 Query Performance Considerations

- If professors list is large, ensure database indexes on `participant.role`, `participant.personDTO.publicId`, and `project.applicationDomainDTO.publicId`.
- The year extraction from `initialSubmission` should be handled in the database WHERE clause (e.g., `YEAR(initialSubmission) >= fromYear`).

### 3.4 Backward Compatibility

- Existing `/projects/public` calls without the new parameters should continue to work.
- Default behavior (no filters applied) should match current behavior.
- All new parameters are optional.

---

## 4. Data Validation

### 4.1 Input Validation

1. **projectTypes**: Validate against enum values `THESIS` or `FINAL_PROJECT`. Ignore invalid values.
2. **applicationDomainIds / professorIds / careerIds**: Parse as comma-separated UUIDs. Use `runCatching` to silently ignore malformed entries.
3. **page / size**: Standard pagination validation. Return `400` if non-numeric.
4. **fromYear / toYear**: Should be valid integers. If provided, validate that `fromYear <= toYear` (optional; may be enforced on client).
5. **search**: Trim whitespace; no special validation required.

### 4.2 Error Handling

- Invalid filters (e.g., non-existent professor IDs) should be **silently ignored**, not return errors.
- Return `200 OK` with empty results if filters are valid but no projects match.
- Return `400 Bad Request` only for structural/type errors in query parameters.

---

## 5. Testing Checklist

- [ ] `/analytics/filters` returns correct metadata including `applicationDomains`.
- [ ] `/projects/public?projectTypes=THESIS` returns only thesis projects.
- [ ] `/projects/public?projectTypes=THESIS,FINAL_PROJECT` returns both types (OR logic).
- [ ] `/projects/public?applicationDomainIds=<uuid>` filters by domain.
- [ ] `/projects/public?professorIds=<uuid>` returns projects where professor is DIRECTOR or CO_DIRECTOR.
- [ ] `/projects/public?fromYear=2020&toYear=2025` filters by year range.
- [ ] Multiple filters combined return only projects matching all criteria (AND logic).
- [ ] Invalid UUIDs are silently ignored.
- [ ] Pagination works correctly with filters applied.
- [ ] Empty search results return `200 OK` with `totalElements: 0`.
- [ ] Backward compatibility: unfiltered requests work as before.

---

## 6. Timeline & Dependencies

- **Frontend Ready:** Yes (PR #20 merged/pending).
- **Backend Dependencies:** None (all required data already in domain model).
- **Estimated Backend Work:** 2–4 hours (query enhancement + caching + testing).

---

## Appendix: Sample Data for Manual Testing

```sql
-- Example: Insert application domains if not already present
INSERT INTO application_domain (id, public_id, name) VALUES 
  (1, '550e8400-e29b-41d4-a716-446655440201', 'Machine Learning'),
  (2, '550e8400-e29b-41d4-a716-446655440202', 'Web Development'),
  (3, '550e8400-e29b-41d4-a716-446655440203', 'Distributed Systems');

-- Verify professor roles include CO_DIRECTOR
SELECT DISTINCT role FROM participant WHERE role IN ('DIRECTOR', 'CO_DIRECTOR');

-- Test year range
SELECT MIN(YEAR(initial_submission)) as minYear, MAX(YEAR(initial_submission)) as maxYear FROM project;
```

---

## Contact & Questions

For clarifications on this spec, reach out to the Frontend team or refer to PR #20 in the repository.
