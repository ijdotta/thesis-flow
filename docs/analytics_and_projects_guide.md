# Analytics Service & Project Data Model Guide

## 1. Analytics Service Overview

The **AnalyticsService** (`src/main/kotlin/ar/edu/uns/cs/thesisflow/analytics/service/AnalyticsService.kt`) is the backbone of the ThesisFlow public analytics portal. It aggregates project data and generates statistical insights, enabling users to explore academic thesis trends through six distinct analytics dimensions:

### 1.1 Analytics Capabilities

The service exposes the following analytics endpoints via **AnalyticsController** (`analytics/api/AnalyticsController.kt`):

1. **Project Timeline** (`getThesisTimeline`)
   - Tracks all project completion trends by professor and year (includes thesis, professional, and other project types).
   - Data structure: professor ID, name, year, and project count.
   - Supports filtering by career, professor, and year range.
   - Frontend visualization: Stacked bar chart (Recharts) showing project progression over time.

2. **Domain Heatmap** (`getTopicHeatmap`)
   - Maps application domain frequency across years.
   - Data structure: domain name, year, and occurrence count.
   - Supports filtering by career and year range.
   - Frontend visualization: Matrix heatmap (ECharts) with dynamic sizing based on domain volume.

3. **Professor Network** (`getProfessorNetwork`)
   - Builds a collaboration graph of professors and their project activity.
   - Node data: professor ID, name, and total project count.
   - Edge data: source professor, target professor, collaboration weight, and count.
   - Supports filtering by career, professor, and year range.
   - Frontend visualization: Interactive network graph (vis-network) with scaled node sizes and edge weights.

4. **Career-Year Statistics** (`getCareerYearStats`)
   - Provides project distribution across careers and years.
   - Data structure: career ID, name, year, and project count.
   - Supports filtering by career and year range.
   - Frontend visualization: Intensity-shaded statistics table showing career performance trends.

5. **Project Type Statistics** (`getProjectTypeStats`)
   - Breaks down projects by type with percentage distribution.
   - Data structure: type name, display name, year, count, and percentage.
   - Supports filtering by career, professor, year range, and application domain.
   - Frontend visualization: Statistics table with type counts and share percentages.

6. **Dashboard Overview** (`getDashboardStats`)
   - High-level metrics summarizing the entire dataset and filter impact.
   - Metrics: total projects, filtered projects, unique domains, unique tags, unique professors, projects with accessible URLs.
   - Supports filtering by career, professor, year range, and application domain; includes configurable top-K results.
   - Frontend visualization: Summary cards (DashboardStats) and top items lists.

7. **Filter Options** (`getFilters`)
   - Exposes available filtering dimensions to the frontend.
   - Returns: careers, professors, year range, and project types.
   - Enables the AnalyticsFilters component to build dynamic filter controls.

### 1.2 Implementation Pattern

Each analytics operation follows the **Command Pattern** (`analytics/command/`):
- Commands are injected into the service and execute independently.
- Each command performs its own repository queries and aggregation logic.
- Data aggregation happens **in-memory** (suitable for current dataset sizes; database-level aggregation recommended for scaling).
- Results are returned as DTOs matching the frontend's expected schema.

### 1.3 Performance Considerations

- Analytics queries load all matching projects into memory and compute aggregations dynamically.
- For large datasets (thousands of projects), consider:
  - Database-level aggregations using native SQL or Hibernate projections.
  - Caching analytics results with time-based invalidation (Redis, Spring Cache).
  - Pagination or sampling for top-K computations.

---

## 2. Project Data Model

### 2.1 Project Entity Structure

The **Project** entity (`projects/persistance/entity/Project.kt`) is the central domain object:

```kotlin
@Entity
class Project(
    val title: String,
    val type: ProjectType,                          // THESIS, PROFESSIONAL, etc.
    val subType: MutableSet<ProjectSubType>,        // TYPE_1, TYPE_2, etc.
    val initialSubmission: LocalDate,               // When the project was submitted
    val completion: LocalDate?,                     // When the project was completed (nullable)
    val career: Career,                             // Associated academic career
    val applicationDomain: ApplicationDomain?,      // Problem domain (optional)
    val tags: MutableSet<Tag>,                      // Topics/keywords (many-to-many)
    val participants: MutableSet<ProjectParticipant>,  // Students, advisors, professors
    val resources: String = "[]"                    // Serialized JSON array of external links
) : BaseEntity()
```

### 2.2 Key Relationships

1. **Career** (many-to-one)
   - Every project belongs to one career (e.g., Computer Science, Software Engineering).
   - Used for filtering and categorization.

2. **ApplicationDomain** (many-to-one, optional)
   - Associates a project with a problem domain (e.g., Healthcare, Finance, Education).
   - Nullable; not all projects have a domain.

3. **Tags** (many-to-many)
   - Multiple tags per project; multiple projects per tag.
   - Represents topics/research areas and feeds the topic heatmap.

4. **ProjectParticipant** (one-to-many)
   - Models relationships between projects and people.
   - Stores participant role (ADVISOR, STUDENT, PROFESSOR, etc.).
   - Enables professor-project association for magic login filtering.

### 2.3 Project Metadata & Auditing

All projects inherit from **BaseEntity** (`common/persistence/BaseEntity.kt`):
- `id`: Auto-incremented numeric primary key (internal use).
- `publicId`: UUID for external identifiers (stable across backups/migrations).
- `createdAt`: Immutable timestamp (JPA @CreatedDate).
- `updatedAt`: Modifiable timestamp (JPA @LastModifiedDate).

### 2.4 Project Lifecycle

1. **Creation**: Manual entry via admin console (`POST /projects`) or bulk import (`POST /bulk/dataset/projects`).
2. **Modification**: Admin or professor can update fields: tags, completion date, participants, application domain.
3. **Filtering**: Public APIs filter by career, professor, year, domain, and type.
4. **Archival**: Soft-delete not implemented; projects are permanently retained (see backup restoration for recovery).

### 2.5 Resources Field

The `resources` field is a JSON-serialized array of external links:

```json
[
  {
    "url": "https://github.com/...",
    "type": "GitHub"
  },
  {
    "url": "https://example.com/paper.pdf",
    "type": "PDF"
  }
]
```

Used by dashboard stats to count projects with accessible URLs and for frontend display.

---

## 3. Frontend Data Statistics & Visualizations

### 3.1 Data Statistics Computed in Frontend

The frontend (`thesisflow-fe/src/pages/public/`) computes and displays:

1. **Total Projects Count**: Displayed in DashboardStats; updated when filters change.
2. **Filtered Projects Count**: Shows impact of applied filters.
3. **Unique Domains**: Count of distinct application domains in filtered dataset.
4. **Unique Topics/Tags**: Count of distinct tags across filtered projects.
5. **Unique Professors**: Count of distinct professors involved in filtered projects.
6. **Projects with Accessible URLs**: Subset of projects with non-empty resources array.

### 3.2 Visualization Libraries

#### ECharts (`echarts@6.0.0`)
- **Purpose**: Rich, interactive charting library.
- **Used for**:
  - Domain Heatmap: Matrix visualization with color intensity mapping application domain-year frequencies.
  - Flexible configuration for tooltips, legends, and animations.
- **Implementation**: `src/hooks/useECharts.ts` wraps ECharts instance initialization.
- **Ref**: [ECharts Documentation](https://echarts.apache.org/)

#### Recharts (`recharts@3.3.0`)
- **Purpose**: Composable React charting library built on D3.
- **Used for**:
  - Project Timeline: Stacked bar chart displaying all projects per professor per year.
  - Responsive container that adapts to viewport width.
  - Accessible tooltips and legends.
- **Implementation**: `src/pages/public/charts/TimelineChart.tsx` uses BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer.
- **Ref**: [Recharts Documentation](https://recharts.org/)

#### vis-network (`vis-network@10.0.2`)
- **Purpose**: Network graph visualization library.
- **Used for**:
  - Professor Network: Interactive graph showing professor nodes and collaboration edges.
  - Dynamic node sizing by project count; edge width by collaboration frequency.
  - Physics simulation for organic layout; user can drag, zoom, and pan.
- **Implementation**: `src/pages/public/charts/ProfessorNetwork.tsx` initializes Network instance and manages graph data.
- **Ref**: [vis-network Documentation](https://visjs.github.io/vis-network/)

### 3.3 Chart Components

- **TimelineChart.tsx**: Project progression over time; stacked by professor.
- **TopicsHeatmap.tsx**: Application domain frequency matrix; dynamic height scaling.
- **ProfessorNetwork.tsx**: Interactive collaboration graph.
- **StatsTable.tsx**: Career year statistics with intensity shading.
- **DashboardStats.tsx**: Overview metrics and top items.
- **AnalyticsFilters.tsx**: Filter panel with dropdowns for career, professor, year range.

---

## 4. Authentication & Authorization

### 4.1 User-Based Admin Login

Admin authentication uses **username + password** (BCrypt hashed):

- **Endpoint**: `POST /auth/login`
- **Payload**: `{ "username": "...", "password": "..." }`
- **Response**: JWT token (contains `userId`, `role: ADMIN`, optionally `professorId`).
- **Implementation**: `AuthController.kt` and `AuthService.kt`.
- **Security**: Stateless; JWT validated on each request by `JwtAuthenticationFilter`.

Admins have access to:
- Full CRUD on projects, people, careers, domains, tags.
- Backup creation and restoration.
- Bulk import operations.

### 4.2 Professor Magic Login

Professors log in via **email-based magic links**:

1. **Request Link**: `POST /auth/professor/request-login-link`
   - Payload: `{ "email": "prof@example.com" }`
   - Backend generates a one-time token (`ProfessorLoginToken`).
   - Email sent via Spring Mail (SMTP).

2. **Verify Link**: `POST /auth/professor/verify-login-link`
   - Payload: `{ "token": "...", "verificationCode": "..." }`
   - Token validated; JWT issued with `role: PROFESSOR` and linked `professorId`.

3. **Project Filtering**: Professors see only their own projects.
   - Frontend filters requests with `?professor.id={professorId}`.
   - Backend `ProjectController` applies JPA subqueries to restrict data.

**Implementation**: `ProfessorAuthController.kt`, `ProfessorAuthService.kt`, `EmailService.kt`.

---

## 5. Technology Stack Summary

### 5.1 Backend Stack

| Layer | Technology | Version | Purpose |
|-------|-----------|---------|---------|
| Framework | Spring Boot | 3.x | Web framework, REST API |
| Language | Kotlin | 1.9 | Type-safe JVM language |
| Database | PostgreSQL | (managed) | Persistent data store |
| Migrations | Flyway | 10 | Schema version control |
| ORM | Spring Data JPA | 3.x | Object-relational mapping |
| Security | Spring Security + jjwt | 5.x / 0.12.x | Authentication, JWT tokens |
| Email | Spring Mail | 3.x | Magic link delivery |
| Testing | JUnit5, MockK, Spring Test | 5.x / 1.x | Automated testing |

### 5.2 Frontend Stack

| Layer | Technology | Version | Purpose |
|-------|-----------|---------|---------|
| Framework | React | 19 | UI library |
| Build Tool | Vite | 7.x | Fast module bundler |
| Language | TypeScript | 5.x | Type-safe JavaScript |
| HTTP Client | Axios | 1.12 | API requests; React Query integration |
| State/Caching | React Query | 5.x | Server state synchronization |
| Routing | React Router | 7.x | Client-side navigation |
| UI Components | shadcn (Radix UI) | various | Accessible, unstyled components |
| Styling | Tailwind CSS | 4.x | Utility-first CSS framework |
| Charts | ECharts, Recharts, vis-network | 6.0, 3.3, 10.0 | Data visualizations |
| Icons | Lucide React | 0.544 | Icon set |

### 5.3 Infrastructure

| Component | Technology | Purpose |
|-----------|-----------|---------|
| Container | Docker | Consistent deployment environment |
| Platform | Render | PaaS deployment (render.yaml) |
| Environment Config | application.yml | Spring profiles (dev/prod) |
| Environment Secrets | Render environment variables | JWT secret, DB credentials, SMTP config |

---

## 6. Backend Analytics Implementation Details

### 6.1 Analytics Commands

Located in `analytics/command/`:

- **GetThesisTimelineCommand**: Queries all projects grouped by professor and year.
- **GetTopicHeatmapCommand**: Aggregates application domain frequencies per year.
- **GetProfessorNetworkCommand**: Builds collaboration edges from project participants.
- **GetCareerYearStatsCommand**: Projects per career per year.
- **GetProjectTypeStatsCommand**: Percentage distribution by project type.
- **GetDashboardStatsCommand**: High-level overview metrics.
- **GetFiltersCommand**: Available filter options.

### 6.2 Data Access Layer

- **ProjectRepository** (`ProjectRepository.kt`): Spring Data JPA repository with custom queries.
- **ProjectSpecifications** (`ProjectSpecifications.kt`): JPA Criteria API specifications for dynamic filtering.
- Filter criteria support:
  - Career filtering: `career.id IN (...)`
  - Professor filtering: `participants.role = ADVISOR AND participants.person.id = ...`
  - Year range: `YEAR(initialSubmission) BETWEEN ... AND ...`
  - Application domain: `applicationDomain.id IN (...)`

### 6.3 Aggregation Logic

Commands load filtered projects into memory and perform aggregations:

```kotlin
// Pseudocode example: Timeline aggregation
projects
    .filter { matchesFilters(it) }
    .groupBy { it.professor }
    .groupBy { it.year }
    .mapValues { count }
    .toDTO()
```

For scaling, consider:
- **Native SQL queries**: Aggregate at database level.
- **Materialized views**: Pre-compute and refresh analytics tables.
- **Caching**: Cache results with TTL (e.g., invalidate hourly).

---

## 7. Backend Project API

### 7.1 Project CRUD Endpoints

**Authenticated Admin & Professor**:
- `GET /projects` - List projects (paginated, filterable)
- `GET /projects/{id}` - Fetch project details
- `POST /projects` - Create new project
- `PUT /projects/{id}` - Update project
- `DELETE /projects/{id}` - Delete project (hard delete)

**Professor-specific**:
- Frontend auto-filters with `?professor.id={professorId}` when role=PROFESSOR.

### 7.2 Project Mutation Endpoints

Dedicated endpoints for common updates:
- `PUT /projects/{id}/tags` - Update tags
- `PUT /projects/{id}/application-domain` - Set application domain
- `PUT /projects/{id}/participants` - Manage participants
- `PUT /projects/{id}/completion-date` - Update completion date

### 7.3 Bulk Operations

- `POST /bulk/dataset/projects` - Import CSV projects (admin only)
- `POST /backup/create` - Export all data as JSON (admin only)
- `POST /backup/restore` - Restore from JSON backup (admin only)

---

## 8. Frontend Project Management

### 8.1 Admin Console Features

Located in `src/pages/admin/`:

- **ProjectsTable**: List all projects; pagination, sorting, filtering by career/type/status.
- **ProjectEditor**: Form-based creation/editing with:
  - Career selection (required)
  - Type and subtypes dropdown
  - Date pickers (initial submission, completion)
  - Multi-select for tags
  - Multi-select for participants
  - Application domain selection
  - Resources/links textarea (JSON editing)

### 8.2 Data Synchronization

- **useProjects() hook**: Fetches `/projects` with React Query caching.
- **Invalidation**: After mutation, React Query refetches data automatically.
- **Filters**: Frontend applies local filtering on client; backend also supports server-side filters for efficiency.

### 8.3 Public Project View

Located in `src/pages/public/`:

- **ProjectList**: Read-only display of filtered projects.
- **ProjectDetails**: Full project view with metadata, participants, tags, resources.
- Filters applied: career, professor, year range, application domain, project type.

---

## 9. Integration Examples

### 9.1 Creating an Analytics Feature (Backend to Frontend)

1. **Backend**: Create a new command in `analytics/command/NewFeatureCommand.kt`.
2. **DTO**: Add response class to `analytics/dto/AnalyticsDTO.kt`.
3. **Service**: Add method to `AnalyticsService` calling the command.
4. **Controller**: Expose via `AnalyticsController` REST endpoint.
5. **Frontend**: Add hook in `src/hooks/` calling the endpoint.
6. **Visualization**: Create chart component using ECharts, Recharts, or vis-network.
7. **Filter Integration**: Update `AnalyticsFilters` and query params if needed.

### 9.2 Adding Project Fields

1. **Database**: Create Flyway migration adding column to `project` table.
2. **Entity**: Add property to `Project.kt`.
3. **DTO**: Extend `ProjectDTO` and `ProjectResource` to include new field.
4. **API**: Controller methods automatically serialize new field.
5. **Frontend**: Update form in ProjectEditor and display in tables/details.

---

## 10. References & Resources

- **Backend API Specification**: `docs/BE_API_REFERENCE.md`
- **Public Analytics API**: `docs/BE_PUBLIC_API_SPEC.md`
- **Analytics Feature Spec**: `docs/BE_ANALYTICS_SPEC.md`
- **Professor Magic Login**: `docs/FE_PROFESSOR_MAGIC_LOGIN_SPEC.md`
- **Backup & Restoration**: `docs/BACKUP_FE_SPEC.md`
- **System Architecture**: `docs/system_overview.md`

