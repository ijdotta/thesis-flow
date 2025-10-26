# Backend Analytics API Specification

This document defines the data models and endpoints needed for the public-facing analytics dashboard. These endpoints should be **publicly accessible** (no authentication required) but should respect data visibility rules (e.g., professors see only projects they're involved in when authenticated).

---

## Overview

The analytics dashboard consists of four main visualizations:
1. **Timeline Chart** – Number of theses per professor over time
2. **Heatmap** – Topic popularity over time
3. **Relationship Graph** – Professor collaboration network
4. **Statistics Table** – Project count per career per year

All endpoints accept optional filters for **career**, **date range**, and **professor** (where applicable).

---

## Common Query Parameters

All analytics endpoints should support these optional filters:

```
GET /analytics/<endpoint>?careerIds=<uuid>,<uuid>&fromYear=2020&toYear=2025&professorIds=<uuid>,<uuid>
```

| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `careerIds` | string (comma-separated UUIDs) | Filter by one or more careers | `careerIds=uuid1,uuid2` |
| `fromYear` | integer | Filter from year (inclusive) | `fromYear=2020` |
| `toYear` | integer | Filter to year (inclusive) | `toYear=2025` |
| `professorIds` | string (comma-separated UUIDs) | Filter by professors (for some endpoints) | `professorIds=uuid1` |

---

## Endpoint 1: Timeline – Thesis per Professor Over Time

**Endpoint:**
```
GET /analytics/thesis-timeline
```

**Query Parameters:**
- `careerIds` (optional) – filter by careers
- `fromYear` (optional) – default to earliest year in DB
- `toYear` (optional) – default to current year
- `professorIds` (optional) – filter by specific professors

**Response:**
```json
{
  "data": [
    {
      "professorId": "uuid-1",
      "professorName": "Dr. Juan García",
      "year": 2020,
      "count": 5
    },
    {
      "professorId": "uuid-1",
      "professorName": "Dr. Juan García",
      "year": 2021,
      "count": 8
    },
    {
      "professorId": "uuid-2",
      "professorName": "Dr. María López",
      "year": 2020,
      "count": 3
    },
    {
      "professorId": "uuid-2",
      "professorName": "Dr. María López",
      "year": 2021,
      "count": 6
    }
  ]
}
```

**Notes:**
- Only count **THESIS** projects (filter `type == "THESIS"`).
- Extraction: use `initialSubmission` year or `completion` year (clarify with BE).
- Each professor row appears once per year they have theses.
- Sort by professor name, then year ascending.

---

## Endpoint 2: Heatmap – Topic Popularity Over Time

**Endpoint:**
```
GET /analytics/topic-heatmap
```

**Query Parameters:**
- `careerIds` (optional)
- `fromYear` (optional)
- `toYear` (optional)

**Response:**
```json
{
  "data": [
    {
      "topic": "Inteligencia Artificial",
      "year": 2020,
      "count": 12
    },
    {
      "topic": "Inteligencia Artificial",
      "year": 2021,
      "count": 18
    },
    {
      "topic": "Blockchain",
      "year": 2020,
      "count": 3
    },
    {
      "topic": "Blockchain",
      "year": 2021,
      "count": 7
    },
    {
      "topic": "Governance",
      "year": 2021,
      "count": 5
    }
  ]
}
```

**Notes:**
- Count projects by tag name and year of `initialSubmission` (or `completion`).
- Include all projects (THESIS + FINAL_PROJECT).
- If a project has multiple tags, count it once per tag.
- Sort by topic name alphabetically, then year ascending.
- Frontend renders as a matrix: topics (rows) × years (columns) with color intensity = count.

---

## Endpoint 3: Professor Collaboration Network

**Endpoint:**
```
GET /analytics/professor-network
```

**Query Parameters:**
- `careerIds` (optional)
- `fromYear` (optional)
- `toYear` (optional)

**Response:**
```json
{
  "nodes": [
    {
      "id": "uuid-1",
      "name": "Dr. Juan García",
      "projectCount": 15
    },
    {
      "id": "uuid-2",
      "name": "Dr. María López",
      "projectCount": 12
    },
    {
      "id": "uuid-3",
      "name": "Dr. Carlos Rodríguez",
      "projectCount": 8
    }
  ],
  "edges": [
    {
      "source": "uuid-1",
      "target": "uuid-2",
      "weight": 5,
      "collaborations": 5
    },
    {
      "source": "uuid-1",
      "target": "uuid-3",
      "weight": 2,
      "collaborations": 2
    },
    {
      "source": "uuid-2",
      "target": "uuid-3",
      "weight": 3,
      "collaborations": 3
    }
  ]
}
```

**Notes:**
- **Nodes**: Each professor appears once with their total project count (as DIRECTOR, CO_DIRECTOR, or COLLABORATOR).
- **Edges**: Created when two professors worked on the same project. `weight` = number of shared projects.
- Only include professors with at least 1 project in the filtered date range.
- Do not include duplicate edges (undirected graph).
- Include professors from all roles: DIRECTOR, CO_DIRECTOR, COLLABORATOR.

---

## Endpoint 4: Statistics Table – Projects per Career per Year

**Endpoint:**
```
GET /analytics/career-year-stats
```

**Query Parameters:**
- `careerIds` (optional)
- `fromYear` (optional)
- `toYear` (optional)

**Response:**
```json
{
  "data": [
    {
      "careerId": "uuid-career-1",
      "careerName": "Ingeniería en Sistemas",
      "year": 2020,
      "projectCount": 25
    },
    {
      "careerId": "uuid-career-1",
      "careerName": "Ingeniería en Sistemas",
      "year": 2021,
      "projectCount": 32
    },
    {
      "careerId": "uuid-career-2",
      "careerName": "Licenciatura en Informática",
      "year": 2020,
      "projectCount": 18
    },
    {
      "careerId": "uuid-career-2",
      "careerName": "Licenciatura en Informática",
      "year": 2021,
      "projectCount": 21
    }
  ]
}
```

**Notes:**
- Count all projects (THESIS + FINAL_PROJECT) per career per year.
- Use `initialSubmission` year (or clarify with BE which date to use).
- Sort by career name, then year ascending.
- Frontend renders as a pivot table or detailed table with sortable columns.

---

## Endpoint 5: Available Filters (Metadata)

**Endpoint:**
```
GET /analytics/filters
```

**Response:**
```json
{
  "careers": [
    {
      "id": "uuid-1",
      "name": "Ingeniería en Sistemas"
    },
    {
      "id": "uuid-2",
      "name": "Licenciatura en Informática"
    }
  ],
  "professors": [
    {
      "id": "uuid-prof-1",
      "name": "Dr. Juan García"
    },
    {
      "id": "uuid-prof-2",
      "name": "Dr. María López"
    }
  ],
  "yearRange": {
    "minYear": 2010,
    "maxYear": 2025
  }
}
```

**Notes:**
- Use this to populate filter dropdowns in the UI.
- Professors list can be restricted to those with at least 1 project.

---

## Endpoint 6: Browse Projects (Public)

**Endpoint:**
```
GET /projects/public
```

**Query Parameters:**
- `careerIds` (optional)
- `professorIds` (optional)
- `fromYear` (optional)
- `toYear` (optional)
- `search` (optional) – search by title or tags
- `page` (optional, default: 0)
- `size` (optional, default: 20)

**Response:**
```json
{
  "content": [
    {
      "publicId": "uuid-project-1",
      "title": "Localizador de Dispositivos Móviles",
      "type": "FINAL_PROJECT",
      "initialSubmission": "2011-06-14",
      "completion": "2011-09-14",
      "career": {
        "publicId": "uuid-career-1",
        "name": "Ingeniería en Sistemas"
      },
      "applicationDomainDTO": {
        "publicId": "uuid-domain-1",
        "name": "Desarrollo Mobile"
      },
      "tags": [
        {
          "publicId": "uuid-tag-1",
          "name": "Desarrollo Mobile"
        }
      ],
      "participants": [
        {
          "role": "DIRECTOR",
          "personDTO": {
            "publicId": "uuid-prof-1",
            "name": "Juan",
            "lastname": "García"
          }
        },
        {
          "role": "STUDENT",
          "personDTO": {
            "publicId": "uuid-student-1",
            "name": "Pablo",
            "lastname": "Fullana"
          }
        }
      ]
    }
  ],
  "totalElements": 245,
  "totalPages": 13,
  "currentPage": 0,
  "size": 20
}
```

**Notes:**
- Reuse existing `GET /projects` logic but **without authentication**.
- Return only public fields (no sensitive admin data).
- Support pagination.

---

## Error Handling

All analytics endpoints should return standard error responses:

| Status | Scenario |
|--------|----------|
| `200 OK` | Success |
| `400 Bad Request` | Invalid query parameters (e.g., `toYear < fromYear`) |
| `500 Internal Server Error` | Unexpected server error |

---

## Implementation Notes

1. **Date Resolution**: Clarify with BE whether to use `initialSubmission` or `completion` for year extraction. Recommend `initialSubmission` for consistency.

2. **Caching**: Consider caching analytics responses (1-hour TTL) as these are read-heavy, computationally expensive queries.

3. **Public Access**: All endpoints should be publicly accessible (no `Authorization` header required) but should be performant.

4. **Filtering**: All parameters are optional; omitting them returns global statistics.

5. **Normalization**: Tags and topics should be case-normalized (e.g., "IA" vs "Inteligencia Artificial" – clarify normalization rules).

---

## Frontend Integration Flow

1. Fetch `/analytics/filters` on dashboard load → populate dropdowns
2. User selects filters → fetch all 4 chart endpoints with query params
3. Render:
   - **Timeline**: Recharts LineChart with series per professor
   - **Heatmap**: ECharts matrix heatmap
   - **Network**: Cytoscape force-directed graph
   - **Stats Table**: TanStack Table with career/year rows
4. On filter change → refetch and update all charts

---

## Future Enhancements

- Add endpoint to export analytics as CSV/PDF
- Add time-based trending (moving averages)
- Add drill-down: click on chart element → filter and show related projects
- Add role-based filtering: professors see only their projects
