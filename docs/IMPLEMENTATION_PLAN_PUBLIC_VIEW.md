# Public Access View - Implementation Plan

## Summary

You want to implement a **public-facing analytics dashboard** with two main sections:
1. **Browse Projects** - searchable/filterable project directory
2. **Analytics Dashboard** - 4 data visualizations

All publicly accessible (no authentication required).

---

## Recommended Technology Stack

### Libraries to Install

```bash
npm install recharts echarts use-echarts vis-network
```

| Visualization | Library | Size | Rationale |
|---|---|---|---|
| **Timeline** (Thesis/Prof over time) | `recharts` | 70 KB | Lightweight, perfect for time-series |
| **Heatmap** (Topics over time) | `echarts` + `use-echarts` | 200 KB | Best heatmap support, professional |
| **Network Graph** (Professor collaborations) | `vis-network` | 250 KB | Force-directed layout, interactive |
| **Stats Table** (Career/Year pivot) | TanStack Table (already installed) | 0 KB | Already in project, use shadcn/ui |

**Total bundle impact:** ~520 KB (acceptable for analytics feature)

---

## What You Need From Backend Engineer

### 6 New Public Endpoints (See `BE_PUBLIC_API_SPEC.md`)

1. **`GET /projects/public`** - Browse all projects (paginated, filterable)
2. **`GET /analytics/filters`** - Metadata (available careers, professors, year range)
3. **`GET /analytics/thesis-timeline`** - Thesis count per professor per year
4. **`GET /analytics/topic-heatmap`** - Topic popularity per year
5. **`GET /analytics/professor-network`** - Professor collaboration network (nodes + edges)
6. **`GET /analytics/career-year-stats`** - Project count per career per year

**Key requirement:** All endpoints must be **publicly accessible** (no `Authorization` header needed).

See `BE_PUBLIC_API_SPEC.md` for complete specifications with request/response examples.

---

## Frontend Implementation Plan

### Phase 1: Project Setup (30 min)

- [ ] Checkout master, pull, create new branch: `feat/public-access-analytics`
- [ ] `npm install recharts echarts use-echarts vis-network`
- [ ] Create directory structure:
  ```
  src/pages/public/
  ├── PublicLayout.tsx
  ├── BrowseProjectsPage.tsx
  ├── AnalyticsDashboardPage.tsx
  ├── charts/
  │   ├── TimelineChart.tsx
  │   ├── TopicsHeatmap.tsx
  │   ├── ProfessorNetwork.tsx
  │   └── StatsTable.tsx
  └── AnalyticsContext.tsx
  ```

### Phase 2: Browse Projects Page (1-2 hours)

- [ ] Create `BrowseProjectsPage.tsx`
- [ ] Build filter sidebar (career, professor, date range)
- [ ] Implement project grid/table display
- [ ] Add pagination
- [ ] Wire `GET /projects/public` endpoint
- [ ] Add search functionality

### Phase 3: Analytics Dashboard Shell (1 hour)

- [ ] Create `AnalyticsDashboardPage.tsx`
- [ ] Build shared filter panel (career, professor, year range)
- [ ] Setup route navigation between views
- [ ] Create `AnalyticsContext.tsx` to share filters across charts
- [ ] Add loading/error states

### Phase 4: Timeline Chart (1 hour)

- [ ] Create `TimelineChart.tsx` with Recharts
- [ ] Display thesis count per professor per year
- [ ] Wire `GET /analytics/thesis-timeline`
- [ ] Add interactive tooltips and legend
- [ ] Test with sample data

### Phase 5: Heatmap Chart (1.5 hours)

- [ ] Create `TopicsHeatmap.tsx` with ECharts
- [ ] Display topic popularity matrix
- [ ] Wire `GET /analytics/topic-heatmap`
- [ ] Customize colors and interactivity
- [ ] Add hover tooltips with counts

### Phase 6: Network Graph (1.5 hours)

- [ ] Create `ProfessorNetwork.tsx` with vis-network
- [ ] Display professor collaboration network
- [ ] Wire `GET /analytics/professor-network`
- [ ] Implement interactions (drag, zoom, pan, hover)
- [ ] Size nodes by project count
- [ ] Edge width proportional to collaborations

### Phase 7: Statistics Table (1 hour)

- [ ] Create `StatsTable.tsx` with TanStack Table
- [ ] Display career/year pivot table
- [ ] Wire `GET /analytics/career-year-stats`
- [ ] Add conditional formatting (color intensity by count)
- [ ] Add sorting and totals row

### Phase 8: Polish & Integration (1-2 hours)

- [ ] Responsive design (mobile, tablet, desktop)
- [ ] Loading skeletons
- [ ] Error handling and messages
- [ ] Performance optimization (memoization, lazy loading)
- [ ] Caching strategy (TanStack Query)
- [ ] Test all filters work together

---

## Current Project State

✅ Already have:
- Router structure in place
- TanStack Query for data fetching
- shadcn/ui components
- Tailwind CSS
- TypeScript setup
- Authentication context

❌ Need to add:
- Public routes (separate from authenticated routes)
- Public API client
- New visualization components
- Analytics state management

---

## File Structure to Create

```
src/
├── pages/public/
│   ├── PublicLayout.tsx                    # Shared public layout
│   ├── BrowseProjectsPage.tsx              # Browse projects page
│   ├── AnalyticsDashboardPage.tsx          # Main analytics page
│   ├── charts/
│   │   ├── TimelineChart.tsx               # Line chart (Recharts)
│   │   ├── TopicsHeatmap.tsx               # Heatmap (ECharts)
│   │   ├── ProfessorNetwork.tsx            # Network graph (vis-network)
│   │   └── StatsTable.tsx                  # Pivot table (TanStack Table)
│   ├── filters/
│   │   ├── AnalyticsFilters.tsx            # Shared filter panel
│   │   └── ProjectFilters.tsx              # Browse page filters
│   ├── AnalyticsContext.tsx                # Filter state context
│   └── PublicRouter.tsx                    # Public routes configuration
│
├── api/
│   ├── public.ts                           # Public endpoints
│   └── analytics.ts                        # Analytics endpoints
│
└── hooks/
    ├── usePublicFilters.ts                 # Filter state hook
    └── useAnalyticsData.ts                 # Analytics data fetching
```

---

## Router Changes

Need to modify `src/App.tsx` or main router to:

1. Detect if user is authenticated (check auth context)
2. If authenticated → show authenticated routes (admin/professor views)
3. If NOT authenticated → show public routes with navigation to:
   - `/public/projects` - Browse projects
   - `/public/analytics` - Analytics dashboard
   - Landing page with links to public sections

Example route structure:
```
/                         → Landing/Home
/login                    → Login page
/admin/                   → Admin dashboard (protected)
/admin/projects           → Project management
/admin/people             → People management
/admin/import             → Import data
/public/                  → Public layout
/public/projects          → Browse projects
/public/analytics         → Analytics dashboard
/public/analytics?view=... → Specific chart view (optional)
```

---

## Key Implementation Details

### Analytics Filters Context

```typescript
interface AnalyticsFilters {
  careerIds?: string[]
  professorIds?: string[]
  fromYear?: number
  toYear?: number
}

// Share across all charts
const AnalyticsContext = createContext<{
  filters: AnalyticsFilters
  setFilters: (filters: AnalyticsFilters) => void
}>(null!)
```

### API Client Pattern

```typescript
// src/api/public.ts
export const publicAPI = {
  projects: (filters?: AnalyticsFilters & { search?: string; page?: number }) => 
    get('/projects/public', { params: filters }),
  
  filters: () => 
    get('/analytics/filters'),
  
  thesissTimeline: (filters?: AnalyticsFilters) => 
    get('/analytics/thesis-timeline', { params: filters }),
  
  topicHeatmap: (filters?: AnalyticsFilters) => 
    get('/analytics/topic-heatmap', { params: filters }),
  
  professorNetwork: (filters?: AnalyticsFilters) => 
    get('/analytics/professor-network', { params: filters }),
  
  careerYearStats: (filters?: AnalyticsFilters) => 
    get('/analytics/career-year-stats', { params: filters }),
}
```

### Filter Change Flow

```
User changes filter
  ↓
AnalyticsContext updates
  ↓
All chart components subscribed to context re-fetch data
  ↓
Charts update with new data
```

---

## Performance Considerations

1. **Caching:** Use TanStack Query with:
   - `staleTime: 5 * 60 * 1000` (5 minutes)
   - `cacheTime: 10 * 60 * 1000` (10 minutes)

2. **Memoization:** Wrap chart components with `React.memo`

3. **Code splitting:** Lazy load public pages:
   ```typescript
   const BrowseProjectsPage = lazy(() => import('./BrowseProjectsPage'))
   const AnalyticsDashboardPage = lazy(() => import('./AnalyticsDashboardPage'))
   ```

4. **Chart optimization:** Only re-render when data changes
   - Use `useCallback` for filter change handlers
   - Memoize chart data transformations

---

## Testing Strategy

1. **Unit tests:** Individual chart components with mock data
2. **Integration tests:** Filters affecting all charts
3. **E2E tests:** Full analytics flow with filters
4. **Performance tests:** Load testing with large datasets
5. **Responsive tests:** Mobile, tablet, desktop viewports

---

## Accessibility Checklist

- [ ] Charts have alt text/descriptions
- [ ] Color contrast meets WCAG AA standards
- [ ] Keyboard navigation works for filters
- [ ] Screen reader support for tables
- [ ] Focus indicators visible
- [ ] Semantic HTML used throughout

---

## Estimated Timeline

| Phase | Task | Hours |
|-------|------|-------|
| 1 | Setup & dependencies | 0.5 |
| 2 | Browse page | 1.5 |
| 3 | Analytics shell | 1 |
| 4 | Timeline chart | 1 |
| 5 | Heatmap chart | 1.5 |
| 6 | Network graph | 1.5 |
| 7 | Stats table | 1 |
| 8 | Polish & testing | 2 |
| **TOTAL** | | **10 hours** |

---

## Next Steps

1. **Share BE spec with backend team:** `BE_PUBLIC_API_SPEC.md`
2. **Review library recommendations:** `LIBRARY_RECOMMENDATIONS.md` 
3. **Wait for backend endpoints** to be ready
4. **Once BE ready:** 
   - Checkout new branch
   - Install dependencies
   - Start Phase 1 implementation
   - Test each endpoint as BE develops them

---

## Questions to Clarify with Backend

1. Which date field to use for year extraction? `initialSubmission` or `completion`?
2. Should sparse data be included (e.g., years with 0 projects)?
3. Caching strategy on backend (TTL for analytics)?
4. Maximum year range filtering supported?
5. Response time expectations for large datasets?

---

## Success Criteria

✅ Public site accessible without login
✅ Browse page shows all projects with working filters
✅ All 4 charts render with sample data
✅ Filters affect all charts in real-time
✅ Responsive on mobile/tablet/desktop
✅ Charts are interactive (hover, tooltips)
✅ Network graph allows dragging/panning
✅ Performance acceptable (< 2s initial load)
✅ No console errors
✅ All tests passing

