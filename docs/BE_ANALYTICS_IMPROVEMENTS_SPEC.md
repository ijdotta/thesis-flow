# Backend API Changes for Analytics Improvements

**Date:** 2025-10-28  
**Focus:** Backend API enhancements for analytics dashboard

---

## 1. Summary of Backend Changes Required

| Item | Type | Priority | Scope |
|------|------|----------|-------|
| Fix professor filter (exclude students) | Fix | HIGH | `/analytics/filters` |
| Enhance network graph with project counts | Enhancement | MEDIUM | `/analytics/professor-network` |
| Add project type stats endpoint | New | MEDIUM | New endpoint |
| Add dashboard stats endpoint | New | MEDIUM | New endpoint |

---

## 2. Detailed Backend Changes

### 2.1 FIX: Professor Filter Endpoint
**Endpoint:** `GET /analytics/filters`  
**Priority:** HIGH  
**Issue:** Professors list includes students

**Current Implementation Issue:**
- Query likely returns all users in projects
- No role filtering applied

**Required Fix:**

```kotlin
// In AnalyticsService or FilterService

fun getFilters(): FilterMetadata {
    // ... existing code ...
    
    // FIX: Only return professors with DIRECTOR or CO_DIRECTOR roles
    val professors = projectRepository.findAll()
        .flatMap { it.participants }
        .filter { p -> p.role in listOf(ParticipantRole.DIRECTOR, ParticipantRole.CO_DIRECTOR) }
        .map { p -> p.personDTO }
        .distinctBy { it.publicId }
        .sortedBy { it.name }
        .map { FilterOption(id = it.publicId, name = "${it.name} ${it.lastname}") }
    
    return FilterMetadata(
        careers = getCareers(),
        professors = professors,  // NOW FILTERED
        applicationDomains = getDomains(),
        yearRange = getYearRange()
    )
}
```

**Testing:**
- Verify no users with STUDENT or COLLABORATOR roles appear
- Verify all DIRECTOR and CO_DIRECTOR roles are included
- Test with filters applied

---

### 2.2 ENHANCEMENT: Network Graph with Project Counts
**Endpoint:** `GET /analytics/professor-network`  
**Current File:** (likely) `AnalyticsController.java/kt`  
**Priority:** MEDIUM

**Current Response Structure:**
```json
{
  "nodes": [
    { "id": "uuid", "name": "string", "projectCount": 0 }
  ],
  "edges": [
    { "source": "uuid", "target": "uuid", "weight": 0, "collaborations": 0 }
  ]
}
```

**Required Change:**
Add `projectCount` that reflects actual project count in date range

**Implementation Steps:**

```kotlin
fun getProfessorNetwork(filters: AnalyticsFilters): ProfessorNetworkData {
    val projects = projectRepository.findAll(filters)  // Respects filter criteria
    
    val nodes = projects
        .flatMap { it.participants }
        .filter { p -> p.role in listOf(ParticipantRole.DIRECTOR, ParticipantRole.CO_DIRECTOR) }
        .distinctBy { it.personDTO.publicId }
        .map { participant ->
            // Count projects for THIS professor in the date range
            val profProjectCount = projects
                .count { project ->
                    project.participants.any { 
                        it.personDTO.publicId == participant.personDTO.publicId &&
                        it.role in listOf(ParticipantRole.DIRECTOR, ParticipantRole.CO_DIRECTOR)
                    }
                }
            
            NetworkNode(
                id = participant.personDTO.publicId,
                name = "${participant.personDTO.name} ${participant.personDTO.lastname}",
                projectCount = profProjectCount  // ACTUAL COUNT IN DATE RANGE
            )
        }
    
    val edges = calculateCollaborationEdges(projects)  // Existing logic
    
    return ProfessorNetworkData(nodes = nodes, edges = edges)
}
```

**Key Points:**
- Use `filters` to respect date range, career, domain filters
- Count projects for each professor independently
- Edges still represent collaborations

---

### 2.3 NEW ENDPOINT: Project Type Statistics
**Endpoint:** `GET /analytics/project-type-stats`  
**Method:** GET  
**Auth:** Public  
**Priority:** MEDIUM

**Query Parameters:**
```
page (int, default 0): Page number
size (int, default 20): Results per page
fromYear (int, optional): Filter from year
toYear (int, optional): Filter to year
careerIds (string, optional): Comma-separated UUIDs
professorIds (string, optional): Comma-separated UUIDs
applicationDomainIds (string, optional): Comma-separated UUIDs
```

**Response:**
```json
{
  "data": [
    {
      "projectType": "THESIS",
      "displayName": "Tesis",
      "year": 2023,
      "projectCount": 15,
      "percentage": 65.2
    },
    {
      "projectType": "FINAL_PROJECT",
      "displayName": "Trabajo Final",
      "year": 2023,
      "projectCount": 8,
      "percentage": 34.8
    }
  ]
}
```

**Implementation:**

```kotlin
@GetMapping("/analytics/project-type-stats")
fun getProjectTypeStats(
    @RequestParam(required = false) careerIds: String?,
    @RequestParam(required = false) professorIds: String?,
    @RequestParam(required = false) fromYear: Int?,
    @RequestParam(required = false) toYear: Int?,
    @RequestParam(required = false) applicationDomainIds: String?,
    @RequestParam(defaultValue = "0") page: Int,
    @RequestParam(defaultValue = "20") size: Int
): ResponseEntity<ProjectTypeStatsResponse> {
    
    val filters = AnalyticsFilters(
        careerIds = parseUUIDs(careerIds),
        professorIds = parseUUIDs(professorIds),
        fromYear = fromYear,
        toYear = toYear,
        applicationDomainIds = parseUUIDs(applicationDomainIds)
    )
    
    val projects = projectRepository.findAll(filters)
    
    val statsByTypeAndYear = projects
        .groupBy { it.type to it.initialSubmission.year }
        .map { (key, projectList) ->
            val (type, year) = key
            ProjectTypeStatData(
                projectType = type.name,
                displayName = if (type == ProjectType.THESIS) "Tesis" else "Trabajo Final",
                year = year,
                projectCount = projectList.size,
                percentage = (projectList.size.toDouble() / projects.size * 100).round(1)
            )
        }
        .sortedWith(compareBy({ it.year }, { it.projectType }))
    
    return ResponseEntity.ok(ProjectTypeStatsResponse(data = statsByTypeAndYear))
}
```

**DTO:**
```kotlin
data class ProjectTypeStatData(
    val projectType: String,        // "THESIS" or "FINAL_PROJECT"
    val displayName: String,        // "Tesis" or "Trabajo Final"
    val year: Int,
    val projectCount: Int,
    val percentage: Double
)

data class ProjectTypeStatsResponse(
    val data: List<ProjectTypeStatData>
)
```

---

### 2.4 NEW ENDPOINT: Dashboard Statistics
**Endpoint:** `GET /analytics/dashboard-stats`  
**Method:** GET  
**Auth:** Public  
**Priority:** MEDIUM

**Query Parameters:**
```
Same as /projects/public filters:
- careerIds
- professorIds
- fromYear / toYear
- projectTypes
- applicationDomainIds
- topK (int, default 5, max 20): Number of top items to return
```

**Response:**
```json
{
  "overview": {
    "totalProjects": 150,           // All projects in system
    "filteredProjects": 42,         // With applied filters
    "uniqueDomains": 12,            // Total unique domains
    "uniqueTags": 35,               // Total unique tags
    "uniqueProfessors": 18          // Total unique directors/co-directors
  },
  "topDomains": [
    { "id": "uuid", "name": "Machine Learning", "count": 8 },
    { "id": "uuid", "name": "Web Development", "count": 6 },
    { "id": "uuid", "name": "AI", "count": 5 }
  ],
  "topTags": [
    { "id": "uuid", "name": "Python", "count": 12 },
    { "id": "uuid", "name": "AI", "count": 10 },
    { "id": "uuid", "name": "JavaScript", "count": 8 }
  ],
  "topProfessors": [
    { "id": "uuid", "name": "Dr. Smith", "projectCount": 5 },
    { "id": "uuid", "name": "Dr. Jones", "projectCount": 4 },
    { "id": "uuid", "name": "Dr. Brown", "projectCount": 3 }
  ]
}
```

**Implementation:**

```kotlin
@GetMapping("/analytics/dashboard-stats")
fun getDashboardStats(
    @RequestParam(required = false) careerIds: String?,
    @RequestParam(required = false) professorIds: String?,
    @RequestParam(required = false) fromYear: Int?,
    @RequestParam(required = false) toYear: Int?,
    @RequestParam(required = false) applicationDomainIds: String?,
    @RequestParam(required = false) projectTypes: String?,
    @RequestParam(defaultValue = "5") topK: Int
): ResponseEntity<DashboardStatsResponse> {
    
    val filters = AnalyticsFilters(
        careerIds = parseUUIDs(careerIds),
        professorIds = parseUUIDs(professorIds),
        fromYear = fromYear,
        toYear = toYear,
        applicationDomainIds = parseUUIDs(applicationDomainIds),
        projectTypes = parseProjectTypes(projectTypes)
    )
    
    val allProjects = projectRepository.findAll()  // NO FILTERS
    val filteredProjects = projectRepository.findAll(filters)  // WITH FILTERS
    
    // Overview stats
    val overview = OverviewStats(
        totalProjects = allProjects.size,
        filteredProjects = filteredProjects.size,
        uniqueDomains = filteredProjects.mapNotNull { it.applicationDomainDTO }.distinctBy { it.publicId }.size,
        uniqueTags = filteredProjects.flatMap { it.tags }.distinctBy { it.publicId }.size,
        uniqueProfessors = filteredProjects
            .flatMap { it.participants }
            .filter { it.role in listOf(ParticipantRole.DIRECTOR, ParticipantRole.CO_DIRECTOR) }
            .map { it.personDTO.publicId }
            .distinct()
            .size
    )
    
    // Top domains
    val topDomains = filteredProjects
        .mapNotNull { it.applicationDomainDTO }
        .groupingBy { it.publicId to it.name }
        .eachCount()
        .map { (key, count) ->
            TopItemData(id = key.first, name = key.second, count = count)
        }
        .sortedByDescending { it.count }
        .take(minOf(topK, 20))
    
    // Top tags
    val topTags = filteredProjects
        .flatMap { it.tags }
        .groupingBy { it.publicId to it.name }
        .eachCount()
        .map { (key, count) ->
            TopItemData(id = key.first, name = key.second, count = count)
        }
        .sortedByDescending { it.count }
        .take(minOf(topK, 20))
    
    // Top professors
    val topProfessors = filteredProjects
        .flatMap { it.participants }
        .filter { it.role in listOf(ParticipantRole.DIRECTOR, ParticipantRole.CO_DIRECTOR) }
        .groupingBy { it.personDTO.publicId to "${it.personDTO.name} ${it.personDTO.lastname}" }
        .eachCount()
        .map { (key, count) ->
            TopProfessorData(id = key.first, name = key.second, projectCount = count)
        }
        .sortedByDescending { it.projectCount }
        .take(minOf(topK, 20))
    
    return ResponseEntity.ok(DashboardStatsResponse(
        overview = overview,
        topDomains = topDomains,
        topTags = topTags,
        topProfessors = topProfessors
    ))
}
```

**DTOs:**
```kotlin
data class OverviewStats(
    val totalProjects: Int,
    val filteredProjects: Int,
    val uniqueDomains: Int,
    val uniqueTags: Int,
    val uniqueProfessors: Int
)

data class TopItemData(
    val id: String,
    val name: String,
    val count: Int
)

data class TopProfessorData(
    val id: String,
    val name: String,
    val projectCount: Int
)

data class DashboardStatsResponse(
    val overview: OverviewStats,
    val topDomains: List<TopItemData>,
    val topTags: List<TopItemData>,
    val topProfessors: List<TopProfessorData>
)
```

**Security Notes:**
- All endpoints are public (no auth required)
- topK parameter capped at 20 to prevent excessive data transfer
- Consider adding rate limiting for heavy computation

---

## 3. Database Query Optimization

### New Indexes Recommended:
```sql
-- Improve project filtering performance
CREATE INDEX idx_project_type ON project(type);
CREATE INDEX idx_project_initial_submission ON project(initial_submission);
CREATE INDEX idx_participant_role ON participant(role);
CREATE INDEX idx_participant_person_id ON participant(person_id);
CREATE INDEX idx_project_domain_id ON project(application_domain_id);

-- For tag queries
CREATE INDEX idx_project_tag ON project_tag(project_id, tag_id);
```

---

## 4. API Response Format Consistency

All analytics endpoints should follow this pattern:
```json
{
  "data": [...],
  "meta": {
    "timestamp": "ISO-8601",
    "appliedFilters": {...},
    "pageInfo": {
      "page": 0,
      "size": 20,
      "totalElements": 42,
      "totalPages": 3
    }
  }
}
```

(Optional enhancement for Phase 2)

---

## 5. Testing Requirements

### Unit Tests:
- [ ] Professor filter excludes students
- [ ] Project count calculation correct for date range
- [ ] Top-K calculations correct
- [ ] Percentage calculations correct

### Integration Tests:
- [ ] All filters work correctly in combination
- [ ] Performance acceptable with large datasets (> 10k projects)
- [ ] Response format valid JSON

### Edge Cases:
- [ ] Empty results (0 projects)
- [ ] Single project
- [ ] All same professor
- [ ] Date range with no projects
- [ ] topK > actual items

---

## 6. Performance Considerations

- **Caching:** Consider caching `/analytics/filters` and `/analytics/dashboard-stats` for 5-10 minutes
- **Pagination:** Not needed for network graph (small dataset typically), but stats endpoint can use pagination
- **Aggregation:** Pre-compute stats during off-hours if dataset grows very large
- **Database Queries:** Use appropriate JOINs and indexes to avoid N+1 queries

---

## 7. Deployment Notes

1. Deploy in order:
   - Fix professor filter in `/analytics/filters`
   - Add new endpoints (`/analytics/project-type-stats`, `/analytics/dashboard-stats`)
   - Enhance existing network endpoint

2. No breaking changes to existing endpoints

3. Frontend can use new data progressively (opt-in)

4. Consider feature flags for gradual rollout of new stats

---

## 8. API Migration Path

**Existing Frontend Code:**
- No changes needed immediately
- Can gradually adopt new endpoints

**New Frontend Features:**
- Project type stats: Use new `/analytics/project-type-stats`
- Dashboard stats: Use new `/analytics/dashboard-stats`
- Network graph: Use existing `/analytics/professor-network` with enhanced data

