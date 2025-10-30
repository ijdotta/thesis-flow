# Backend Filtering & Search Requirements

**Audience:** Backend engineers  
**Status:** Ready for implementation  
**Last updated:** 2025-10-30  

This document captures every API contract the frontend relies on for filtering, sorting, and search across admin tables, sheets, and creation wizards. Several UI filters currently return unfiltered data because the backend endpoints do not yet implement the expected query parameters. Implementing the behaviour below will make all table filters and typeahead experiences work consistently.

> **Paging conventions used throughout the FE**
>
> - `page`: zero-based page index (`0` is the first page).
> - `size`: page size (defaults to 25 in most tables).
> - `sort`: formatted as `<field>,<direction>` where `direction` is `asc` or `desc`.
> - Responses must follow the existing Spring-style page envelope:
>   ```json
>   {
>     "content": [ ... ],
>     "totalElements": 123,
>     "totalPages": 5,
>     "size": 25,
>     "number": 0
>   }
>   ```

---

## 1. Admin Tables (GET)

Each admin table sends the filters listed below as query parameters in addition to `page`, `size`, and `sort`. Filters are trimmed strings; empty values are omitted. unless stated otherwise, comparisons should be **case-insensitive contains** matches.

| Endpoint | Filter keys | Expected behaviour |
|----------|-------------|--------------------|
| `GET /projects` | `title`, `type`, `career`, `directors`, `students`, `completion`, `professorId` | - `title`, `career`, `directors`, `students`: substring match on the respective text fields.<br>- `type`: exact match (`THESIS` or `PROJECT`).<br>- `completion`: `true` or `false` strings; interpret as boolean to filter finished vs. in-progress projects.<br>- `professorId`: UUID string; restrict results to projects where the professor participates (FE sets this automatically for the professor role). |
| `GET /professors` | `lastname`, `name`, `email` | Partial match per field. Combine filters with AND semantics. |
| `GET /students` | `lastname`, `name`, `studentId`, `email` | Partial match per field. `studentId` should be an exact or prefix match (backends may choose). |
| `GET /people` | `lastname`, `name` | Partial match per field. |
| `GET /careers` | `name` | Partial match. |
| `GET /application-domains` | `name`, `description` | Partial match. |
| `GET /tags` | `name`, `description` | Partial match. |

### Error handling
- Return `400` if an unsupported filter key is provided.
- Return `200` with an empty `content` array when no rows match.

### Sorting
Ensure the sort fields mirror the filter keys above. When multiple sort fields are allowed, validate and reject invalid fields with `400 Bad Request`.

---

## 2. Typeahead / Search Helpers

Creation wizards and sheets rely on lightweight search endpoints that accept a single `q` parameter. These queries should return the same page envelope used elsewhere (the FE can handle either an array or a paged object) and perform a case-insensitive substring match across sensible fields.

| Endpoint | Description | Requirements |
|----------|-------------|--------------|
| `GET /people?q=` | Search existing people while creating students/professors. | Match against `name`, `lastname`, and optionally email. Limit to 20 results by default. |
| `GET /professors?q=` | Search professors for association in project wizard. | Match against `name`, `lastname`, and email. Include `content`, `totalElements`, etc. |
| `GET /students?q=` | Search students (used in sheets). | Match against name/lastname/studentId. |
| `GET /application-domains?q=` | Search domains for tagging. | Match against `name`/`description`. |
| `GET /tags?q=` | Search tags for tagging UI. | Match against `name`/`description`. |

Return `[]` or an empty page when `q` is missing/blank.

---

## 3. Public Browse & Analytics Endpoints

Public pages share a filter context for analytics and project browsing. All endpoints below expect the same optional query parameters:

| Parameter | Type | Behaviour |
|-----------|------|-----------|
| `careerIds` | comma-separated UUIDs | Restrict results to projects associated with the given careers. |
| `professorIds` | comma-separated UUIDs | Restrict to projects/analytics linked to the specified professors. |
| `fromYear` | integer | Inclusive minimum year (applies to project completion or initial submission depending on dataset). |
| `toYear` | integer | Inclusive maximum year. |
| `search` | string (only for `/projects/public`) | Case-insensitive substring match against project title, description, tags, professors. |
| `page`, `size` | integers (only `/projects/public`) | Same paging contract as admin tables. |

Endpoints:
- `GET /projects/public`
- `GET /analytics/filters` (no filters, just metadata)
- `GET /analytics/thesis-timeline`
- `GET /analytics/topic-heatmap`
- `GET /analytics/professor-network`
- `GET /analytics/career-year-stats`
- `GET /analytics/project-type-stats`
- `GET /analytics/dashboard-stats`

> **Implementation hint:** use consistent parsing helpers so that `careerIds=a,b,c` maps to a Set/array of UUIDs. Ignore duplicates and unknown IDs gracefully.

---

## 4. Response Shape Consistency

To avoid FE fallbacks, please always return:

```json
{
  "content": [],
  "totalElements": 0,
  "totalPages": 0,
  "size": 25,
  "number": 0
}
```

When no records match filters. The FE already handles both paged responses and legacy array responses, but aligning all endpoints to the paged contract reduces complexity.

---

## 5. Validation & Testing Checklist

1. Verify each filter combination returns the expected subset (AND semantics).
2. Confirm pagination metadata reflects filtered totals.
3. Ensure search endpoints respect `q` and are safe against empty queries.
4. Add unit/integration tests covering:
   - Single filter
   - Multiple filters combined
   - Invalid filter keys
   - Pagination edge cases (`page` out of range → empty result).
5. Smoke-test the FE tables after deploying backend changes:
   - Filter by text (surname, title, etc.).
   - Filter by select options (project type, completion).
   - Confirm professor view only shows their own projects.

---

## 6. Future Enhancements (Optional)

- Support advanced search operators (`:` for exact match, `*` wildcard) if needed later.
- Expose available filter keys via `OPTIONS` or schema documentation.
- Add audit logging for restoration actions tied to filtered data changes.

Please reach out to the FE team if any field names differ from backend entity attributes so we can align before implementation.
