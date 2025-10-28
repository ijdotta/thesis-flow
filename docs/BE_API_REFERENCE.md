# Thesis Flow Backend API Reference

_Last reviewed: 2025-10-27_

This document captures the Spring Boot REST API implemented in this repository. It complements the high-level specs in `docs/BE_PUBLIC_API_SPEC.md` by describing every controller, request payload, response shape, and authorization rule as currently coded.

## 1. Authentication & Authorization

- **Login** via `POST /auth/login`. Provide credentials in the request body and receive a JWT.
- Supply the token in `Authorization: Bearer <jwt>` for all protected endpoints.
- Tokens embed the following claims: `sub` (username), `role` (`ADMIN` or `PROFESSOR`), `userId` (public UUID), and optional `professorId`.

### 1.1 Access Rules

Authorization rules are enforced in `auth/config/SecurityConfig.kt`.

| Endpoint pattern | Allowed access |
| --- | --- |
| `/auth/login` | Public |
| `/analytics/**` | Public |
| `/projects/public/**` | Public |
| `PUT /projects/{id}/application-domain`, `PUT /projects/{id}/tags` | Role `ADMIN` or `PROFESSOR` |
| `GET /projects/**` | Role `ADMIN` or `PROFESSOR` |
| All other routes (`/projects` mutations, `/people`, `/students`, `/professors`, `/careers`, `/tags`, `/bulk/**`, etc.) | Role `ADMIN` only |

If authorization fails an `ErrorResponse` with HTTP 403 is returned.

## 2. Common Behaviours

### 2.1 Error Format

All controllers share `common/exceptions/ControllerExceptionHandler.kt`, which renders errors as:

```json
{
  "message": "Human readable message",
  "timestamp": "2025-10-27T01:23:45.678Z",
  "path": "http://localhost:8080/projects/unknown-id"
}
```

### 2.2 Pagination

List endpoints return Spring Data `Page<T>` objects and accept `page` (0-based) and `size` (defaults vary by controller). Serialized pages include keys such as `content`, `pageable`, `totalElements`, `totalPages`, `size`, `number`, `sort`, `first`, `last`, etc.

### 2.3 Date & Time

- `LocalDate` values (e.g., project `initialSubmission`) are serialized as ISO-8601 date strings (`YYYY-MM-DD`).
- `Instant` values (e.g., `LoginResponse.expiresAt`) are serialized as ISO-8601 timestamps with UTC offset.

### 2.4 Enumerations

| Enum | Values | Notes |
| --- | --- | --- |
| `UserRole` | `ADMIN`, `PROFESSOR` | Included in JWT `role` claim and `LoginResponse.role`. |
| `ProjectType` | `THESIS`, `FINAL_PROJECT` | Accepted in `ProjectDTO.type` and filters. |
| `ProjectSubType` | `TYPE_1`, `TYPE_2` | Optional subtype list on projects. |
| `ParticipantRole` | `STUDENT`, `DIRECTOR`, `CO_DIRECTOR`, `COLLABORATOR` | Used in participant arrays and update payloads. |
| `ProjectImportStatus` | `SUCCESS`, `SKIPPED`, `FAILED` | Reported by dataset imports. |

## 3. Data Transfer Objects (DTOs)

Field types reflect Kotlin declarations; all strings are UTF-8 encoded.

### 3.1 Authentication

`LoginRequest`

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `username` | `string` | ✓ | |
| `password` | `string` | ✓ | |

`LoginResponse`

| Field | Type | Notes |
| --- | --- | --- |
| `token` | `string` | JWT to use in `Authorization` header. |
| `expiresAt` | `string` (ISO-8601) | Expiration timestamp. |
| `role` | `string` (`UserRole`) | |
| `userId` | `string` (UUID) | Public identifier of the authenticated user. |
| `professorId` | `string` (UUID) or `null` | Present only for professor accounts. |

### 3.2 Projects & Related Entities

`ProjectDTO`

| Field | Type | Notes |
| --- | --- | --- |
| `publicId` | `string` (UUID) | Populated on reads. |
| `title` | `string` | Required for create/update. |
| `type` | `string` (`ProjectType`) | Required for create/update. |
| `subtype` | `string[]` (`ProjectSubType`) | Optional. |
| `initialSubmission` | `string` (date) | Required. |
| `completion` | `string` (date) or `null` | Optional completion date. |
| `careerPublicId` | `string` (UUID) | Required for create. |
| `career` | `CareerDTO` | Hydrated on reads. |
| `applicationDomainDTO` | `ApplicationDomainDTO` or `null` | Hydrated on reads. |
| `tags` | `TagDTO[]` | Hydrated on reads. |
| `participants` | `ParticipantDTO[]` | Hydrated on reads. |

Supporting DTOs:

- `ApplicationDomainDTO`: `{ publicId?, name?, description? }`
- `TagDTO`: `{ publicId?, name?, description? }`
- `ParticipantDTO`: `{ personDTO: PersonDTO, role: ParticipantRole }`
- `ParticipantInfo`: `{ personId: string (UUID), role: ParticipantRole }` (request payload)
- `SetTagsRequest`: `{ tagIds: string[] }`
- `SetApplicationDomainRequest`: `{ applicationDomainId: string }`
- `SetParticipantsRequest`: `{ participants: ParticipantInfo[] }`

### 3.3 Catalog & People

- `CareerDTO`: `{ id?, publicId?, name }`
- `PersonDTO`: `{ id?, publicId?, name, lastname }`
- `ProfessorDTO`: `{ id?, publicId?, personPublicId?, name?, lastname?, email? }`
- `StudentDTO`: `{ publicId?, personPublicId?, person?, name?, lastname?, studentId?, email?, careers: CareerDTO[] }`
- `UpdateCareersRequest`: `{ careers: string[] (UUID list) }`

### 3.4 Analytics

- `ThesisTimelineResponse`: `{ data: ThesisTimelineData[] }`, where `ThesisTimelineData = { professorId, professorName, year, count }`
- `TopicHeatmapResponse`: `{ data: TopicHeatmapData[] }`, where `TopicHeatmapData = { topic, year, count }`
- `ProfessorNetworkResponse`: `{ nodes: NetworkNode[], edges: NetworkEdge[] }`
  - `NetworkNode = { id, name, projectCount }`
  - `NetworkEdge = { source, target, weight, collaborations }`
- `CareerYearStatsResponse`: `{ data: CareerYearStatsData[] }`
  - `CareerYearStatsData = { careerId, careerName, year, projectCount }`
- `FilterMetadata`: `{ careers: FilterOption[], professors: FilterOption[], applicationDomains: FilterOption[], yearRange: YearRange }`
  - `FilterOption = { id, name }`
  - `YearRange = { minYear, maxYear }`

### 3.5 Bulk Import

`ProjectImportResponse`

| Field | Type | Notes |
| --- | --- | --- |
| `summary` | `ProjectImportSummary` | Aggregated counts. |
| `results` | `ProjectImportResult[]` | Entry per imported row. |

`ProjectImportSummary = { total, success, skipped, failed }`

`ProjectImportResult = { lineNumber: number, title: string?, status: ProjectImportStatus, project: ProjectDTO?, message: string? }`

### 3.6 Error Response

`ErrorResponse = { message: string, timestamp: string, path: string }`

## 4. Endpoint Reference

### 4.1 Public Endpoints

#### POST `/auth/login`

- **Purpose:** Obtain JWT.
- **Auth:** Public.
- **Request body:** `LoginRequest` (JSON).
- **Success response:** `200 OK` with `LoginResponse`.
- **Error codes:** `401` for bad credentials (default Spring Security response).

Example request:

```http
POST /auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "change-me"
}
```

#### GET `/projects/public`

- **Purpose:** Public-facing project listing with filters.
- **Auth:** Public.
- **Query parameters:**

| Name | Type | Notes |
| --- | --- | --- |
| `page` | int | Defaults to `0`. |
| `size` | int | Defaults to `20`. |
| `careerIds` | string (comma-separated UUIDs) | Optional. |
| `professorIds` | string (comma-separated UUIDs) | Directors/co-directors. |
| `projectTypes` | string (comma-separated) | Accepts `THESIS`, `FINAL_PROJECT`. |
| `applicationDomainIds` | string (comma-separated UUIDs) | Filters by application domain. |
| `fromYear` / `toYear` | int | Inclusive filters on `initialSubmission`. |
| `search` | string | Matches title or tag names (case-insensitive). |

- **Response:** `200 OK` with a pagination map `{ content, totalElements, totalPages, currentPage, size }`. Each `content` entry is a `ProjectDTO` with embedded participants limited to public-facing data.

#### GET `/analytics/filters`

- **Purpose:** Fetch cached filter options.
- **Auth:** Public.
- **Response:** `200 OK` with `FilterMetadata`.

#### GET `/analytics/thesis-timeline`
#### GET `/analytics/topic-heatmap`
#### GET `/analytics/professor-network`
#### GET `/analytics/career-year-stats`

- **Purpose:** Public analytics datasets.
- **Auth:** Public.
- **Shared query parameters:** `careerIds`, `fromYear`, `toYear`; timeline and network also accept `professorIds`.
- **Responses:** Respective DTOs described in §3.4.
- **Notes:** Filters are parsed as comma-separated UUIDs; invalid UUIDs are ignored via `runCatching`.

### 4.2 Project Administration (`/projects`)

Unless noted, all routes require the `ADMIN` role; GET routes are also available to `PROFESSOR`.

#### GET `/projects`

- Accepts pagination plus extensive filters:

| Name | Type | Notes |
| --- | --- | --- |
| `title` | string | Case-insensitive contains. |
| `professor.name` / `directors` | string | Comma- or space-separated fragments matched against director/co-director names. |
| `student.name` / `students` | string | Name fragments matched against student participants. |
| `domain` | string | Case-insensitive contains on application domain name. |
| `completed` | boolean | Legacy alias for `completion`. |
| `completion` | boolean | `true` = completion date present, `false` = null. |
| `type` | string | Comma-separated values; synonyms accepted (`PROJECT`, `TESIS`, etc.). |
| `sort` | string | `<field>,<direction>`; allowed fields: `createdAt`, `updatedAt`, `title`, `type`, `initialSubmission`, `completion`. Pseudo fields `students`/`directors` map to `createdAt`. |

- **Response:** `200 OK` with `Page<ProjectDTO>` (enriched with participants, tags, career, application domain).

#### GET `/projects/{id}`

- **Auth:** `ADMIN` or `PROFESSOR`.
- **Response:** `200 OK` with a single enriched `ProjectDTO`.

#### POST `/projects`

- **Auth:** `ADMIN`.
- **Request body:** `ProjectDTO` (must include `title`, `type`, `subtype` if needed, `initialSubmission`, `careerPublicId`; `career`, `participants`, `tags` ignored in create).
- **Response:** `201 Created` (Spring currently returns `200 OK` because controller calls `projectService.create` directly). Body is the created `ProjectDTO`.
- **Errors:** `400` for validation issues (e.g., missing `careerPublicId`).

#### PUT `/projects/{id}`

- **Auth:** `ADMIN`.
- **Request body:** Partial `ProjectDTO` for updates.
- **Response:** `200 OK` with updated `ProjectDTO`.

#### PUT `/projects/{id}/tags`

- **Auth:** `ADMIN` or owning `PROFESSOR` (additional check in `ProjectAuthorizationService`).
- **Request body:** `SetTagsRequest`.
- **Response:** `200 OK` with updated `ProjectDTO`.

#### PUT `/projects/{id}/application-domain`

- **Auth:** `ADMIN` or owning `PROFESSOR`.
- **Request body:** `SetApplicationDomainRequest`.
- **Response:** `200 OK` with updated `ProjectDTO`.

#### PUT `/projects/{id}/participants`

- **Auth:** `ADMIN`.
- **Request body:** `SetParticipantsRequest`. Participant roles must respect business rules (students must belong to project career, etc.); violations yield `400`.
- **Response:** `200 OK` with updated `ProjectDTO`.

#### DELETE `/projects/{id}`

- **Auth:** `ADMIN`.
- **Response:** `204 No Content` (controller currently returns `200 OK` with empty body).

#### POST `/projects/bulk-import`

- **Auth:** `ADMIN`.
- **Consumes:** `multipart/form-data` with `file` part (CSV).
- **Behaviour:** Currently validates presence of a CSV file and forwards to `ProjectService.bulkImportFromCsv`, which returns a placeholder payload `{ "message": "Bulk import not yet implemented", "fileName": "<name>" }`.
- **Responses:**
  - `201 Created` with import summary map.
  - `400 Bad Request` if file empty or not CSV.
  - `500 Internal Server Error` for unexpected exceptions.

### 4.3 Dataset Import (`/bulk/dataset`)

#### POST `/bulk/dataset/projects`

- **Auth:** `ADMIN`.
- **Consumes:** `multipart/form-data` with `file` containing the legacy dataset.
- **Response:** `200 OK` with `ProjectImportResponse`.
- **Validation:** Rejects empty uploads with `400 Bad Request`.

### 4.4 Catalog (`/application-domains`, `/tags`, `/careers`)

All routes require `ADMIN` role.

#### GET `/application-domains` | `/tags` | `/careers`

- Accept `page` & `size`.
- Return `Page<ApplicationDomainDTO>` / `Page<TagDTO>` / `Page<CareerDTO>`.

#### GET `/application-domains/{publicId}` (and analogous for `/tags`, `/careers`)

- Return the DTO by public UUID.
- 404 handling is delegated to repositories (returns `IllegalArgumentException` -> `400` if not found).

#### POST `/application-domains` | `/tags` | `/careers`

- Accept respective DTOs (validation occurs in services).
- Return created DTOs.

#### PUT `/application-domains/{publicId}` | `/tags/{publicId}` | `/careers/{publicId}`

- Update operations with partial DTOs.
- Return updated DTOs.

#### DELETE `/application-domains/{id}` | `/tags/{id}` | `/careers/{id}`

- Delete by public UUID.
- `ApplicationDomain` and `Career` deletions enforce referential checks and return `400` with explanatory message if in use.

### 4.5 People Management (`/people`, `/professors`, `/students`)

Routes require `ADMIN` role.

#### `/people`

- `GET /people`: Returns `Page<PersonDTO>`.
- `POST /people`: Creates a new person; requires `name` and `lastname`. Returns created DTO.
- `PUT /people/{publicId}`: Updates existing person (partial updates allowed). Returns updated DTO.
- `DELETE /people/{id}`: Removes person if not associated with projects; otherwise returns `400`.

#### `/professors`

- `GET /professors`: Returns `Page<ProfessorDTO>`.
- `GET /professors/{publicId}`: Returns single professor DTO.
- `POST /professors`: Requires `personPublicId` and valid institutional `email` (`@cs.uns.edu.ar` or `@uns.edu.ar`). Returns created DTO.
- `PUT /professors/{publicId}`: Updates professor (can re-associate to another person if permitted). Returns updated DTO.

#### `/students`

- `GET /students`: Returns `Page<StudentDTO>` with embedded `careers`.
- `GET /students/{publicId}`: Returns single student DTO.
- `POST /students`: Requires `personPublicId`, `studentId`, and `email`. Returns created DTO.
- `PUT /students/{publicId}`: Partial update for `studentId` / `email`.
- `PUT /students/{publicId}/careers`: Accepts `UpdateCareersRequest` listing career public UUIDs. Returns `StudentDTO` with refreshed `careers`. Validation ensures careers exist; missing IDs trigger `400`.

## 5. Usage Notes for Frontend Engineers

- **Token storage:** `expiresAt` is an absolute timestamp; refresh tokens are not implemented, so prompt re-authentication when expired.
- **Public UI:** Use `/projects/public` and `/analytics/**`; no auth headers required.
- **Admin UI:** After login, include `Authorization` header for all mutations and for `/projects` reads.
- **Error handling:** Always parse the standard `ErrorResponse` for messaging, but note that validation exceptions may surface as `400` with specific text in `message`.
- **CSVs:** `POST /projects/bulk-import` and `/bulk/dataset/projects` expect multipart uploads; ensure the request uses `Content-Type: multipart/form-data` and field name `file`.
