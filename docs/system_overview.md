# ThesisFlow Platform Documentation

## 1. Purpose & Scope
- Describe the complete ThesisFlow solution that digitises DCIC academic project management.
- Capture how the backend, frontend, and database collaborate to deliver public analytics, professor self-service, and internal administration.
- Serve as the baseline document for the forthcoming academic paper (see `docs/document_reference.md` for the expected paper outline).

## 2. High-Level Architecture

### 2.1 Core Components
- **Backend (`thesis-flow`)**: Spring Boot 3 + Kotlin 1.9 service that exposes REST APIs, handles authentication/authorization, manages domain entities, produces analytics, and controls bulk/backup flows. Key build configuration lives in `build.gradle.kts`.
- **Frontend (`thesisflow-fe`)**: React 19 + Vite + TypeScript single-page application that offers both an authenticated admin console and a public-facing analytics portal.
- **Database**: PostgreSQL schema managed by Flyway migrations (`src/main/resources/db/migration`). Uses UUID-based `public_id` columns for external identifiers and numeric primary keys for relations.
- **Infrastructure & Tooling**: Docker image (`Dockerfile`) wraps Gradle build + JVM runtime; deployment blueprint provided via `render.yaml`. Email delivery for professor magic login relies on Spring Mail (`spring.mail.*` in `application.yml`).

### 2.2 Data & Control Flow
1. **Authentication**: Admins use `/auth/login` credentials; professors can request magic links via `/auth/professor/request-login-link`. Successful login yields JWTs used in subsequent requests.
2. **Project Data Lifecycle**:
   - Records originate from CSV imports (`/bulk/dataset/projects`) or manual creation in the admin console.
   - Entities persisted through repositories under `src/main/kotlin/ar/edu/uns/cs/thesisflow/projects/persistance`.
   - Public endpoints (`/projects/public`, `/analytics/**`) surface curated data for external audiences.
3. **Analytics Pipeline**: `AnalyticsService` composes in-memory aggregations (timelines, heatmaps, social graphs, dashboards) from repository queries.
4. **Backups & Restoration**: `BackupService` enumerates table contents into JSON and can rebuild the database in dependency order. Frontend offers guarded workflows to trigger these tasks.
5. **Frontend Rendering**: React Query fetches data from the backend, caches it, and drives a mixture of shadcn-inspired UI components, ECharts, Recharts, and vis-network visualisations.

## 3. Backend (Spring Boot + Kotlin)

### 3.1 Technology Stack
- Spring Boot starters for Web, Data JPA, Security, Validation, Mail (`build.gradle.kts:14-37`).
- Flyway 10 for schema migrations (`build.gradle.kts:25-28`).
- JWT handled by `io.jsonwebtoken` (jjwt) dependencies.
- Database driver: PostgreSQL runtime dependency.
- Test support: Spring Boot Test, H2, MockK, AssertJ, Spring Security Test.

### 3.2 Package Overview & Responsibilities

#### 3.2.1 Application Bootstrap
- Entry point `ThesisFlowApplication.kt` starts the Spring context.
- `Config.kt` sets global CORS allowing requests from the Vite dev host (`http://localhost:5173`).
- `config/JpaAuditingConfig.kt` enables auditing annotations used on entities (tracks `createdAt`/`updatedAt`).

#### 3.2.2 Common Infrastructure
- `common/persistence/BaseEntity.kt` defines the default identity pattern: auto-increment `id` plus immutable `publicId: UUID`. All entities extend this class to guarantee consistent equality semantics.
- `common/exceptions/ControllerExceptionHandler.kt` provides uniform error payloads (message/timestamp/path) and centralises logging across controllers.

#### 3.2.3 Authentication & Authorization (`auth/*`)
- **Models**: `AuthUser` (`auth/model/AuthUser.kt`) stores usernames, BCrypt-hashed passwords, roles (`UserRole` enum), and optional professor linkage.
- **Security config**: `auth/config/SecurityConfig.kt` wires HttpSecurity, JWT filter, stateless sessions, and request matchers:
  - Public: `/auth/login`, `/auth/professor/**`, `/analytics/**`, `/projects/public/**`.
  - Authenticated (Admin or Professor): mutate/read projects (`/projects/**`), read-only for tags/careers/domains/students, restricted writes to admins.
  - Admin-only: backup endpoints, catalog mutations, bulk import.
- **JWT lifecycle**: `JwtService` (`auth/service/JwtService.kt`) issues tokens with role, userId, and optional professorId claims; `JwtAuthenticationFilter` attaches authenticated principals for downstream checks.
- **Magic login flow**:
  - `ProfessorAuthController.kt` exposes request/verify endpoints.
  - `ProfessorAuthService.kt` generates random tokens, stores them in `ProfessorLoginToken` table, rate-limits requests, sends HTML emails (`EmailService.kt`), and produces JWTs upon verification.
- **Utility services**: `CurrentUserService` helps modules inspect the logged-in user (role, professor public id).

#### 3.2.4 Catalog & People Management
- **Careers**: `catalog/persistance/entity/Career.kt` + repository/service/controller trio handles CRUD with guard rails (cannot delete if students exist).
- **People**: `people/persistance/entity/*` define `Person`, `Student`, `StudentCareer`, `Professor`.
  - Services enforce invariants: e.g., `ProfessorService` demands institutional email domains, prevents double-linking persons.
  - `StudentService` orchestrates person creation, career associations (`StudentCareerRepository`), and validations.
  - REST endpoints live in `people/api/*.kt`, each layered over pageable queries + dynamic filters (see `ProfessorSpecifications`, `StudentSpecifications`, etc.).

#### 3.2.5 Project & Tag Domain (`projects/*`)
- **Entities**:
  - `Project.kt` associates careers, tags, application domains, participants, type/subtype enumerations.
  - `ProjectParticipant.kt` enforces unique combination of project-person-role; participants reference `Person`, not higher-level wrappers.
  - `Tag.kt` & `ApplicationDomain.kt` hold taxonomy data.
- **Services**:
  - `ProjectService.kt` centralises CRUD, enriches participants, manages tags/domains/completion dates, enforces professor-level authorization via `ProjectAuthorizationService.kt`.
  - Bulk import entry point (`bulkImportFromCsv`) is stubbed with TODO notes (backend already supports legacy import via dedicated module).
- **Controllers**:
  - `ProjectController.kt` provides administrative APIs with extensive query filters (title, professor name/id, student name, domain/career, completion, type) and sorting support.
  - `PublicProjectController.kt` exposes `/projects/public` with explicit filter composition and manual paging to avoid leaking internal details (only exposes `ParticipantDTO`).
  - `TagController.kt` and `ApplicationDomainController.kt` mirror career patterns (paging, filtering, admin-protected mutations).
  - `SetTagsRequest`, `SetApplicationDomainRequest`, `SetParticipantsRequest`, `SetCompletionDateRequest` define specific RPC-style payloads.
- **Specifications**: `ProjectSpecifications.kt` builds dynamic JPA criteria queries, including subselects for participant-based filters and multi-type handling.

#### 3.2.6 Analytics (`analytics/*`)
- `AnalyticsService.kt` produces the dataset consumed by the public analytics front end:
  - Thesis timelines (projects per professor & year, filtered by careers/professors/years).
  - Topics heatmap (tag-year matrix).
  - Professor collaboration network (Nodes = professors, edges weighted by shared projects).
  - Career-year stats, project type distributions, dashboard overviews (top domains/tags/professors, counts).
  - Filter metadata (careers, directors/co-directors, year range).
- `AnalyticsController.kt` exposes `/analytics/...` endpoints translating query strings into UUID lists and delegating to the service.

#### 3.2.7 Backup & Restore (`backup/*`)
- `BackupService.kt` uses `EntityManager` to dump database rows into DTOs, with deterministic table order, and to restore them with referential integrity preserved. It deletes existing data via `DELETE_ORDER` to avoid FK issues.
- `BackupController.kt` (not shown above but part of the package) maps to `/backup/create` and `/backup/restore`.
- DTOs under `backup/dto/BackupDtos.kt` describe serialised forms for each table, ensuring there are no lazy-loading pitfalls.

#### 3.2.8 Bulk Import (`bulk/*`)
- `DatasetImportController.kt` receives CSV uploads at `/bulk/dataset/projects`, hands them to `LegacyDatasetImporter`, and returns structured results (`ProjectImportSummary`, `ProjectImportResult`).
- `LegacyDatasetImporter.kt` reads `data/dataset.csv`, normalises data, persists careers/tags/domains if missing, links participants, and categorises outcomes (SUCCESS/SKIPPED/FAILED) with rollback protections through `TransactionTemplate`.
- `DatasetImportRunner.kt` optionally auto-imports on application startup when `app.dataset.import.enabled=true`.

### 3.3 Persistence Schema
- `V1__init_schema.sql` defines core tables (`person`, `career`, `student`, `professor`, `project`, `project_tags`, `project_participant`, `student_career`, `auth_user`) with UUID `public_id` columns and supporting indexes/constraints.
- `V2__seed_data.sql` inserts initial seed data: sample persons, careers, domains, tags, students/professors, two projects, project participants, and default admin credentials (BCrypt hash in `auth_user` table).
- `V3__add_professor_login_tokens.sql` introduces the `professor_login_token` table to support magic link authentication.

### 3.4 Configuration & Deployments
- `application.yml` centralises environment-sensitive settings (database URL/credentials, JWT secret/expiration, mail server, dataset import flag). All values read from environment variables with safe defaults.
- `application-prod.yml` keeps prod-specific Flyway & schema validation toggles.
- `Dockerfile` uses a two-stage build (Gradle builder + Temurin JRE runtime), ships a non-root `appuser`, registers a health check on `/actuator/health`, and exposes port 8080.
- `render.yaml` configures Render.com deployment: sets the Docker runtime, environment variables (with placeholders for secrets), and retains Flyway migrations.

### 3.5 Notable Business Flows
- **Professor project edits**: `ProjectAuthorizationService.ensureCanModify` cross-checks the logged-in professor with project participants (must be director/co-director).
- **Magic link lifecycle**:
  1. Professor submits email ➜ validated + rate-limited (`ProfessorAuthService.checkRateLimit`).
  2. Token persisted (`ProfessorLoginToken`) with expiry + single-use guarantees.
  3. Email sent via `EmailService`, linking back to frontend `/professor-login/verify`.
  4. Verification endpoint consumes token, ensures valid state, creates/injects an `AuthUser` if missing, and returns JWT.
- **Backup restore safeguards**: `BackupService.restoreBackup` clears all tables, repopulates in dependency order, flushes, and wraps exceptions in a `RestoreException` to surface meaningful errors.
- **Analytics filters** ensure only directors/co-directors appear in drop-downs, aligning with public reporting goals.

### 3.6 Gaps / TODOs
- `ProjectService.bulkImportFromCsv` still contains TODO comments; existing CSV import flows use the dedicated `/bulk/dataset` controller.
- Tag/application domain APIs currently trust admin role at HTTP layer; extra guardrails (e.g., duplicate names) rely on unique constraints but may warrant richer validation.
- `LegacyDatasetImporter` uses simple heuristics for type mapping; consider externalising to configuration for broader dataset compatibility.

## 4. Frontend (React + Vite)

### 4.1 Technology Choices
- React 19 with functional components/hooks, TypeScript strict mode.
- Routing via `react-router-dom` 7, state/data via `@tanstack/react-query`.
- UI composition: shadcn-inspired primitives (`src/components/ui`), `lucide-react` icons, Sonner toasts, Tailwind classes (configured through `tailwindcss` & `@tailwindcss/vite`).
- Visualisations: Recharts (`TimelineChart`), Apache ECharts (`TopicsHeatmap`), vis-network (`ProfessorNetwork`).
- Build tooling: Vite (`vite.config.ts`), ESLint + TypeScript ESLint for linting, Vitest for unit tests (tests not yet populated).

### 4.2 Application Shell & Routing
- Entry (`src/main.tsx`) wraps the app in `ErrorBoundary`, `AuthProvider`, `QueryClientProvider`, and `BrowserRouter`.
- Routes:
  - Public root (`/`) handled by `PublicRouter` ➜ `PublicLayout`.
  - Auth pages: `/login`, `/professor-login`, `/professor-login/verify`.
  - Admin console tree under `/admin/*`, dispatched by `RoleBasedRouter`.
- `RoleBasedRouter.tsx` inspects current user (`useAuth`) and maps roles to pages:
  - Admin: projects, people, professors, students, careers, application domains, tags, backup, import.
  - Professor: limited to the projects view (auto-filtered to their assignments).

### 4.3 Authentication UX
- `AuthContext.tsx` persists JWTs in `localStorage`, decodes payloads for role/user IDs, exposes `login`, `logout`, `requestProfessorMagicLink`, and `verifyProfessorMagicLink`.
- `api/axios.ts` attaches `Authorization: Bearer` headers automatically and redirects to `/login` on 401 responses; deduplicates error toasts to avoid floods.
- **Admin login** (`LoginPage.tsx`) provides a polished form with Sonner notifications on failure.
- **Professor magic link** pages:
  - `ProfessorLoginPage.tsx` validates institutional emails, handles resend cooldown (60s), and guides the user through the request flow.
  - `ProfessorLoginVerifyPage.tsx` processes the `token` query parameter, calls `verifyProfessorMagicLink`, shows progress and success/failure states, and redirects to dashboards.

### 4.4 Public Experience
- **Layout (`PublicLayout.tsx`)**: sticky header with navigation to projects/analytics, login/logout controls, responsive design, and footer metadata.
- **Home (`PublicHomePage.tsx`)**: hero content, feature cards, placeholder stats (to be wired to live metrics).
- **Projects (`BrowseProjectsPage.tsx`)**:
  - Uses `publicAPI.browseProjects` with filters from shared context.
  - Provides search bar, tag chips, participants preview, pagination controls, and `ProjectDetailDialog` to show full metadata.
  - `ProjectDetailDialog` (from admin components) reuses detail presentation with copy-to-clipboard summary.
- **Analytics (`AnalyticsDashboardPage.tsx`)**:
  - Wraps charts in `AnalyticsProvider` which stores filters (careers, professors, year range).
  - `AnalyticsFilters.tsx` fetches metadata (`/analytics/filters`), presents collapsible sections with checkboxes/sliders, and supports clearing.
  - Visual widgets: `TimelineChart`, `TopicsHeatmap`, `ProfessorNetwork`, `StatsTable`, `ProjectTypeStats`, `DashboardStats`.
  - Each chart component handles loading/error states and composes data structures expected by the underlying charting library.

### 4.5 Admin Console Features
- **Layout**: `AdminLayout.tsx` uses `SidebarProvider` + `AppSidebar.tsx` to present navigation (different menus for admins/professors) and integrates a logout action with identity summary.
- **Reusable table toolkit**:
  - `DataTable.tsx` powers all resource lists: supports pagination, sorting, filter inputs (text/select), keyboard shortcuts, filter chips, and debounced updates.
  - Hooks like `useProjects`, `usePagedPeople`, `usePagedCareers` wrap API calls & react-query state.
- **Projects module**:
  - `ProjectTable.tsx` combines `DataTable` with advanced modals: `ProjectViewSheet`, `ProjectTagsSheet`, `ProjectCompletionSheet`, and `ProjectManageSheet`.
  - `CreateProjectWizard.tsx` orchestrates multi-step creation (basic info, staff, students, summary) with reusable subcomponents under `components/projectWizard/*`.
  - Wizard actions (`actions.ts`) persist temporary people/professors/students before creating the project, ensuring referential integrity and transactional rollback on failure.
  - Professors see the same UI but `useProjects` automatically injects `professorId` filter to constrain results.
- **Reference data tables**:
  - `PeopleTable.tsx`, `ProfessorsTable.tsx`, `StudentsTable.tsx`, `CareersTable.tsx`, `ApplicationDomainsTable.tsx`, `TagsTable.tsx` follow an identical pattern: list, filter, open sheet to create/update, deletion guarded by confirmation dialogs. Each component uses the corresponding API module (`src/api/people.ts`, etc.).
- **Backup Manager (`features/backup/BackupManager.tsx`)**:
  - Multi-step restore workflow (warning ➜ automatic safety backup ➜ arithmetic challenge ➜ JSON validation ➜ final confirmation).
  - Validates table presence, summarises record counts, formats sizes, triggers downloads via `api/backup.ts`.
  - Uses rich state machines (`RestoreStep`, `AsyncStatus`) to give user feedback; `REQUIRED_TABLES` ensures schema alignment.
- **Import Data (`ImportDataPage.tsx`)**:
  - Accepts CSV uploads, validates size/type, calls `parseCsv` (backend bulk import).
  - Displays row-level results with status badges (success/skipped/failed) and allows removing problematic rows en masse.
  - Summaries update live as rows are removed; future step could persist curated results (currently parse endpoint performs import).

### 4.6 Shared Utilities & Hooks
- `src/api/*`: typed wrappers around backend endpoints (projects, people, students, careers, application domains, tags, backup, import, analytics public APIs). Consistent error handling via axios interceptor.
- `src/hooks/*`: search helpers (`useSearchProfessors`, `useSearchStudents`), debouncing (`useDebounce`), charts (`useECharts`), pagination state, React Query wrappers.
- `src/mapper/responseToProjectMapper.ts`: normalises backend DTOs into UI-friendly `Project` structures (e.g., mapping participant roles to lists, translating enum values into labels).
- `src/components/ui` houses primitive components (button, card, input, table, badge, tooltip, sheet, sidebar, skeleton) to keep design consistent.
- `contexts` folder contains typed context definitions and hooks (`AuthContextDefinition.ts`, `useAuth.ts`).

## 5. Backend ↔ Frontend Integration
- **Authentication**:
  - Admin login posts to `/auth/login` and persists JWT via `AuthContext`.
  - Magic link flows call `/auth/professor/request-login-link` then `/auth/professor/verify-login-link`, aligning with `ProfessorAuthService`.
- **Resource APIs**:
  - Admin tables consume `/projects`, `/people`, `/professors`, `/students`, `/careers`, `/application-domains`, `/tags`; each supports pagination, filters, and updates.
  - Project mutations hit dedicated endpoints for tags (`PUT /projects/{id}/tags`), application domain, participants, completion date—mirroring backend request DTOs.
  - Backup UI invokes `/backup/create` (GET blob) and `/backup/restore` (POST JSON).
  - Import page uploads to `/bulk/dataset/projects` (`multipart/form-data`), processing the same shape described in `docs/BE_API_REFERENCE.md`.
- **Public APIs**:
  - `publicAPI` module targets `/projects/public` and analytics endpoints; results feed charts and cards without requiring authentication.
  - Filters produced by backend (`FiltersResponse`) power the AnalyticsFilters component.
- **Professor Project Filtering**:
  - Frontend `useProjects` merges `professorId` filter when role=PROFESSOR; backend `ProjectController` interprets `professor.id` query parameter via JPA subqueries to restrict data.

## 6. Data & Analytics Features
- **Timeline**: Computed by `AnalyticsService.getThesisTimeline` and rendered in `TimelineChart` via stacked bars grouped by professor/year.
- **Topic Heatmap**: Tag-year frequencies transformed into matrix data for ECharts (`TopicsHeatmap.tsx`), with dynamic height to accommodate variable tag counts.
- **Professor Network**: Backend returns nodes (professors + projectCount) and edges (collaboration counts). Frontend uses vis-network to scale node sizes by activity and edge width by collaboration frequency.
- **Career Stats**: `CareerYearStatsResponse` becomes the heatmap-like table in `StatsTable.tsx`, with intensity shading.
- **Project Type Stats**: Backend return percentages ensure table cells show counts and share of filtered data, complementing the overall dashboard summary in `DashboardStats.tsx`.
- **Dashboard Overview**: Combined metrics (total vs filtered projects, domains, tags/topics, unique professors) highlight dataset breadth and filter impact.

## 7. Operational Considerations
- **Environment setup**:
  - Backend expects PostgreSQL credentials (defaults to local `thesis_flow_owner` user) and SMTP settings for email.
  - `APP_DATASET_IMPORT_ENABLED` toggles automatic import at startup.
  - JWT secrets should be replaced in production.
- **Local development**:
  - Backend: `./gradlew bootRun` (ensure `application.yml` defaults fit local DB).
  - Frontend: `npm install` + `npm run dev` inside `thesisflow-fe`; environment variable `VITE_API_BASE_URL` should target backend origin (defaults to same host if unset).
- **Deployment**:
  - Use provided Dockerfile; Render manifest indicates stateless deployment with managed DB & secrets.
  - Health check monitors `/actuator/health`; ensure actuator is enabled if adding security.
- **Email**: Magic link pipeline requires functional SMTP credentials (Mailtrap defaults included for dev). Failures raise runtime exceptions (see `EmailService.kt`).

## 8. Known Limitations & Future Work
- **Bulk Import**: Admin UI currently displays parse results only; actual mass application relies on backend importer logic. Consider extending UI to allow selective persistence or rollback.
- **Project Subtypes**: Mapping in `responseToProjectMapper.ts` expects specific enum values; backend `ProjectSubType` currently uses placeholders (`TYPE_1`, `TYPE_2`). Align naming or expose richer metadata.
- **Analytics Efficiency**: Current analytics recompute by loading all projects into memory. For growing datasets, consider database-level aggregations or caching layers.
- **Authorization Granularity**: Service-level checks exist (e.g., professor project ownership), but additional row-level security may be worthwhile, especially for people/career endpoints.
- **Testing Coverage**: Repository includes test dependencies but lacks comprehensive unit/integration tests. Prioritise coverage for critical flows (auth, analytics, backup) before production rollout.
- **Frontend App Shell**: `src/App.tsx` still holds Vite starter code; cleaning up avoids confusion and reduces bundle size.

## 9. Cross-Reference Resources
- API specifics: `docs/BE_API_REFERENCE.md`, `docs/BE_PUBLIC_API_SPEC.md`.
- Feature specs & plans: `docs/BE_ANALYTICS_SPEC.md`, `docs/FE_PROFESSOR_MAGIC_LOGIN_SPEC.md`, `docs/IMPLEMENTATION_PLAN_PUBLIC_VIEW.md`, `docs/READY_TO_IMPLEMENT.md`.
- Backup behaviour: `docs/BACKUP_FE_SPEC.md`.
- For paper structure: `docs/document_reference.md`.

---
This document should act as the canonical source while drafting the academic paper and during onboarding. Update it alongside major architectural or feature changes to keep the narrative accurate.
