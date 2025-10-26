# Backend Public Analytics API Specification

This document defines all endpoints needed for the public-facing analytics dashboard and browse page. **All endpoints are publicly accessible** (no Authorization header required).

---

## Overview

The public API provides two main features:

1. **Browse Projects** - searchable, filterable project directory
2. **Analytics Dashboard** - 4 visualizations of trends, topics, collaborations, and statistics

All analytics endpoints accept optional filters for **career**, **date range**, and **professor**.

---

## Common Query Parameters

All analytics endpoints support these optional filters:

```
GET /endpoint?careerIds=uuid1,uuid2&fromYear=2020&toYear=2025&professorIds=uuid1,uuid2
```

| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `careerIds` | string (comma-separated UUIDs) | Filter by one or more careers | `careerIds=uuid1,uuid2` |
| `fromYear` | integer | Start year (inclusive) | `fromYear=2020` |
| `toYear` | integer | End year (inclusive) | `toYear=2025` |
| `professorIds` | string (comma-separated UUIDs) | Filter by professors | `professorIds=uuid1,uuid2` |

---

## Endpoint 1: Browse Projects (Paginated)

**Purpose:** List all projects with filtering and search.

```
GET /projects/public
```

### Query Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `careerIds` | string | No | Comma-separated career UUIDs |
| `professorIds` | string | No | Comma-separated professor UUIDs |
| `fromYear` | int | No | Start year (inclusive) |
| `toYear` | int | No | End year (inclusive) |
| `search` | string | No | Search by title, tags, or topics |
| `page` | int | No | Page number (0-indexed), default 0 |
| `size` | int | No | Page size, default 20, max 100 |

### Response (200 OK)

```json
{
  "content": [
    {
      "publicId": "9d1d4e51-c900-46c9-a95f-2a267ce7c427",
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

---

## Endpoint 2: Available Filters (Metadata)

**Purpose:** Fetch available filter options to populate UI dropdowns.

```
GET /analytics/filters
```

### Response (200 OK)

```json
{
  "careers": [
    {
      "id": "uuid-career-1",
      "name": "Ingeniería en Sistemas"
    },
    {
      "id": "uuid-career-2",
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

---

## Endpoint 3: Timeline - Thesis per Professor Over Time

**Purpose:** Render line chart showing number of theses per professor per year.

```
GET /analytics/thesis-timeline
```

### Query Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `careerIds` | string | Comma-separated career UUIDs |
| `professorIds` | string | Comma-separated professor UUIDs |
| `fromYear` | int | Start year |
| `toYear` | int | End year |

### Response (200 OK)

```json
{
  "data": [
    {
      "professorId": "uuid-prof-1",
      "professorName": "Dr. Juan García",
      "year": 2020,
      "count": 5
    },
    {
      "professorId": "uuid-prof-1",
      "professorName": "Dr. Juan García",
      "year": 2021,
      "count": 8
    },
    {
      "professorId": "uuid-prof-2",
      "professorName": "Dr. María López",
      "year": 2020,
      "count": 3
    }
  ]
}
```

### Notes

- Count only THESIS projects
- Extract year from `initialSubmission` date
- Sort by professor name, then year ascending

---

## Endpoint 4: Heatmap - Topic Popularity Over Time

**Purpose:** Render heatmap showing topic popularity per year.

```
GET /analytics/topic-heatmap
```

### Query Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `careerIds` | string | Comma-separated career UUIDs |
| `fromYear` | int | Start year |
| `toYear` | int | End year |

### Response (200 OK)

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
    }
  ]
}
```

### Notes

- Count projects by tag name and year of `initialSubmission`
- Include ALL projects (THESIS + FINAL_PROJECT)
- If a project has multiple tags, count it once per tag
- Sort by topic name alphabetically, then year ascending

---

## Endpoint 5: Network Graph - Professor Collaborations

**Purpose:** Render network graph of professor collaborations.

```
GET /analytics/professor-network
```

### Query Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `careerIds` | string | Comma-separated career UUIDs |
| `professorIds` | string | Filter to specific professors |
| `fromYear` | int | Start year |
| `toYear` | int | End year |

### Response (200 OK)

```json
{
  "nodes": [
    {
      "id": "uuid-prof-1",
      "name": "Dr. Juan García",
      "projectCount": 15
    },
    {
      "id": "uuid-prof-2",
      "name": "Dr. María López",
      "projectCount": 12
    }
  ],
  "edges": [
    {
      "source": "uuid-prof-1",
      "target": "uuid-prof-2",
      "weight": 5,
      "collaborations": 5
    }
  ]
}
```

### Notes

- Each professor appears once with total project count (DIRECTOR, CO_DIRECTOR, COLLABORATOR)
- Edges created when two professors worked on same project
- Undirected graph (no duplicate edges)
- Include professors from all roles

---

## Endpoint 6: Statistics Table - Projects per Career per Year

**Purpose:** Render pivot table of project counts per career per year.

```
GET /analytics/career-year-stats
```

### Query Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `careerIds` | string | Comma-separated career UUIDs |
| `fromYear` | int | Start year |
| `toYear` | int | End year |

### Response (200 OK)

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
    }
  ]
}
```

### Notes

- Count all projects (THESIS + FINAL_PROJECT)
- Extract year from `initialSubmission` date
- Sort by career name, then year ascending

---

## HTTP Status Codes

| Status | Meaning |
|--------|---------|
| `200 OK` | Success |
| `400 Bad Request` | Invalid parameters |
| `500 Internal Server Error` | Server error |

---

## Implementation Checklist

- [ ] Endpoint 1: GET /projects/public (browse + pagination)
- [ ] Endpoint 2: GET /analytics/filters (career/professor/year options)
- [ ] Endpoint 3: GET /analytics/thesis-timeline (thesis per prof per year)
- [ ] Endpoint 4: GET /analytics/topic-heatmap (topics per year)
- [ ] Endpoint 5: GET /analytics/professor-network (collaboration network)
- [ ] Endpoint 6: GET /analytics/career-year-stats (stats per career per year)

All endpoints should be publicly accessible and return pre-aggregated data (frontend just renders).
