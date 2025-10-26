# Analytics Dashboard - Library Recommendations & Rationale

## Executive Summary
For the public access analytics dashboard with 4 visualizations + browse page, we recommend:
- **Timeline (Thesis/Professor)**: `recharts`
- **Heatmap (Topics over time)**: `echarts` + `use-echarts` hook
- **Network Graph (Professor collaborations)**: `vis-network` (via `vis-network` + custom React wrapper)
- **Stats Table (Career/Year pivot)**: Native TanStack Table + shadcn/ui

**Total packages to add: 4**

---

## Detailed Analysis & Decisions

### 1. Timeline Chart - Thesis Per Professor Over Time

**Requirement:**
- Line chart, multi-series (one line per professor)
- X-axis: Years | Y-axis: Project count
- Interactive tooltips, legends
- Responsive

**Recommended: `recharts`**

```bash
npm install recharts
```

**Why Recharts:**
- ✅ Lightweight, composable React components
- ✅ Perfect for time-series data
- ✅ Built-in legends, tooltips, responsive
- ✅ Excellent TypeScript support
- ✅ 24.5k GitHub stars, actively maintained
- ✅ Small bundle size (~70KB gzipped)
- ✅ No external dependencies

**Alternative considered: `echarts`**
- ❌ Heavier bundle (~200KB gzipped)
- ❌ Steeper learning curve for this simple use case
- ✅ Would be overkill for just a line chart

**Rejected: `plotly.js`**
- ❌ Much larger bundle
- ❌ Overkill for simple line chart

---

### 2. Heatmap - Topic Popularity Over Time

**Requirement:**
- 2D heatmap/matrix visualization
- X-axis: Years | Y-axis: Topics (sorted by popularity)
- Color intensity ∝ project count
- Interactive (hover shows count)

**Recommended: `echarts` + `use-echarts` hook**

```bash
npm install echarts use-echarts
```

**Why ECharts:**
- ✅ Best-in-class heatmap component (native support)
- ✅ Beautiful, professional visualizations
- ✅ Excellent interactivity & tooltips
- ✅ Lightweight for heatmaps (~50KB additional for heatmap component)
- ✅ Strong TypeScript support
- ✅ Huge ecosystem (23k GitHub stars)
- ✅ Used by Fortune 500 companies

**Why `use-echarts` hook:**
- ✅ Thin React wrapper over echarts
- ✅ Simplifies option management & resize handling
- ✅ No bloat, just convenience

**Alternative considered: Plotly.js**
- ❌ Much heavier bundle for just heatmap
- ✅ More feature-rich but unnecessary

**Alternative considered: Custom CSS grid**
- ✅ Lightweight
- ❌ No interactivity, harder to maintain

---

### 3. Relationship Graph - Professor Collaborations

**Requirement:**
- Network graph: nodes (professors) + weighted edges (collaborations)
- Node size ∝ project count
- Edge width ∝ collaboration count
- Force-directed layout with physics simulation
- Drag, zoom, pan, hover interactions

**Recommended: `vis-network`**

```bash
npm install vis-network
```

**Why vis-network:**
- ✅ Built specifically for network/graph visualization
- ✅ Force-directed layout with physics simulation (automatic!)
- ✅ Excellent interaction model (drag, zoom, pan)
- ✅ Built-in hover effects
- ✅ Lightweight & performant (handles 1000+ nodes)
- ✅ 9.5k GitHub stars, stable
- ✅ No external D3 complexity
- ✅ Simple node/edge styling API

**Custom React wrapper needed:**
```typescript
// Optional: lightweight wrapper for React integration
import { Network } from 'vis-network';

export const VisNetwork = ({ nodes, edges, options }) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const networkRef = useRef<Network | null>(null);

  useEffect(() => {
    if (containerRef.current) {
      networkRef.current = new Network(containerRef.current, { nodes, edges }, options);
    }
  }, [nodes, edges, options]);

  return <div ref={containerRef} style={{ width: '100%', height: '600px' }} />;
};
```

**Alternatives considered:**
- `cytoscape.js`: More graph-theory focused, heavier, steeper learning curve
- `d3.js`: Massive learning curve, requires manual force simulation setup
- `react-force-graph`: Interesting but less mature, heavier than vis-network

---

### 4. Statistics Table - Career/Year Pivot

**Requirement:**
- Pivot table: Rows=Careers, Columns=Years, Cells=Project count
- Sortable, conditional formatting (color intensity)
- Totals row/column

**Recommended: Native TanStack Table + shadcn/ui + Tailwind**

```bash
npm install @tanstack/react-table
# Already available: shadcn/ui, tailwindcss
```

**Why TanStack Table (React Table):**
- ✅ Already installed in project
- ✅ Headless (full control, pairs perfectly with shadcn/ui)
- ✅ Lightweight (~8KB gzipped)
- ✅ Excellent TypeScript support
- ✅ Sorting, filtering, pagination built-in
- ✅ No styling lock-in (use Tailwind)

**Why NOT MUI DataGrid:**
- ❌ Heavy, opinionated styling
- ❌ Overkill for this use case
- ❌ Bundle bloat

**Conditional formatting:** Use Tailwind utility classes with dynamic color:

```tsx
<td className={`
  bg-${intensity}-50 
  text-${intensity}-900 
  font-semibold
`}>
  {count}
</td>
```

---

## Bundle Impact Analysis

| Library | Size (gzipped) | Purpose | Risk |
|---------|---|---------|------|
| `recharts` | ~70 KB | Timeline chart | Low - widely used, stable |
| `echarts` | ~200 KB | Heatmap & utilities | Low - battle-tested, industry standard |
| `use-echarts` | ~2 KB | ECharts React hook | Low - thin wrapper |
| `vis-network` | ~250 KB | Network graph | Low - mature, performant |
| **TOTAL** | **~522 KB** | - | - |

**Context:** Your current app is likely ~200-300 KB. Adding ~522 KB is acceptable for a rich analytics experience.
- Browsers cache aggressively
- Code can be lazy-loaded if needed later

---

## Implementation Strategy

### Phase 1: Setup & Dependency Installation (30 min)
- [ ] `npm install recharts echarts use-echarts vis-network`
- [ ] Create `/src/pages/public/` directory structure
- [ ] Setup API clients for public endpoints
- [ ] Create types for analytics data

### Phase 2: Browse Projects Page (1-2 hours)
- [ ] Create `BrowseProjectsPage.tsx`
- [ ] Implement filters UI (career, professor, date range)
- [ ] Wire `GET /projects/public` endpoint
- [ ] Render projects grid/table

### Phase 3: Analytics Dashboard Skeleton (1 hour)
- [ ] Create `AnalyticsDashboardPage.tsx`
- [ ] Build shared filter panel
- [ ] Setup route navigation between views
- [ ] Create analytics context for state sharing

### Phase 4: Timeline Chart (1 hour)
- [ ] Create `TimelineChart.tsx` with Recharts
- [ ] Wire `GET /analytics/thesis-timeline`
- [ ] Test with sample data

### Phase 5: Heatmap Chart (1.5 hours)
- [ ] Create `TopicsHeatmap.tsx` with ECharts
- [ ] Wire `GET /analytics/topic-heatmap`
- [ ] Style & customize appearance

### Phase 6: Network Graph (1.5 hours)
- [ ] Create `ProfessorNetwork.tsx` with vis-network
- [ ] Wire `GET /analytics/professor-network`
- [ ] Implement interactions (hover, click)

### Phase 7: Stats Table (1 hour)
- [ ] Create `StatsTable.tsx` with TanStack Table
- [ ] Wire `GET /analytics/career-year-stats`
- [ ] Add conditional formatting

### Phase 8: Polish (1-2 hours)
- [ ] Responsive design
- [ ] Loading states
- [ ] Error handling
- [ ] Performance optimization

**Total estimated time: 8-10 hours**

---

## Backend API Endpoints Needed

All endpoints should be **publicly accessible** (no auth header required):

### 1. `GET /projects/public`
Browse all projects (paginated, filterable)

```
Query params:
  ?careerIds=uuid1,uuid2
  &professorIds=uuid1
  &fromYear=2020
  &toYear=2025
  &search=keyword
  &page=0
  &size=20

Response:
{
  "content": [ProjectDTO...],
  "totalElements": 150,
  "totalPages": 8,
  "currentPage": 0,
  "size": 20
}
```

### 2. `GET /analytics/filters`
Available filter options (careers, professors, year range)

```
Response:
{
  "careers": [{ "id": "uuid", "name": "..." }...],
  "professors": [{ "id": "uuid", "name": "..." }...],
  "yearRange": { "minYear": 2010, "maxYear": 2025 }
}
```

### 3. `GET /analytics/thesis-timeline`
Thesis count per professor per year

```
Query params:
  ?careerIds=uuid1,uuid2
  &professorIds=uuid1
  &fromYear=2020
  &toYear=2025

Response:
{
  "data": [
    {
      "professorId": "uuid",
      "professorName": "Dr. Juan García",
      "year": 2020,
      "count": 5
    }...
  ]
}
```

### 4. `GET /analytics/topic-heatmap`
Topic popularity per year (count matrix)

```
Query params: same as above

Response:
{
  "data": [
    {
      "topic": "Inteligencia Artificial",
      "year": 2020,
      "count": 12
    }...
  ]
}
```

### 5. `GET /analytics/professor-network`
Professor collaboration network (nodes & edges)

```
Query params: same as above

Response:
{
  "nodes": [
    { "id": "uuid", "name": "Dr. Juan García", "projectCount": 15 }...
  ],
  "edges": [
    { "source": "uuid1", "target": "uuid2", "weight": 5 }...
  ]
}
```

### 6. `GET /analytics/career-year-stats`
Project count per career per year

```
Query params: same as above

Response:
{
  "data": [
    {
      "careerId": "uuid",
      "careerName": "Ingeniería en Sistemas",
      "year": 2020,
      "projectCount": 25
    }...
  ]
}
```

---

## Frontend Project Structure (Proposed)

```
src/pages/public/
├── PublicLayout.tsx              # Layout with nav
├── BrowseProjectsPage.tsx        # Main browse page
│   ├── ProjectFilters.tsx
│   └── ProjectGrid.tsx
├── AnalyticsDashboardPage.tsx    # Main analytics page
│   ├── AnalyticsFilters.tsx      # Shared filters
│   ├── charts/
│   │   ├── TimelineChart.tsx     # Recharts
│   │   ├── TopicsHeatmap.tsx     # ECharts
│   │   ├── ProfessorNetwork.tsx  # vis-network
│   │   └── StatsTable.tsx        # TanStack Table
│   └── AnalyticsContext.tsx      # Filter state
└── ProjectDetailPage.tsx         # Single project (future)

src/api/
├── public.ts                     # Public API endpoints
└── analytics.ts                  # Analytics endpoints

src/hooks/
├── usePublicFilters.ts           # Filter state hook
└── useAnalyticsData.ts           # Data fetching hook
```

---

## Performance Considerations

1. **Caching:** Use TanStack Query with 5-minute stale time
2. **Virtualization:** Add if 1000+ projects (use `react-window` only if needed)
3. **Lazy loading:** Code-split public routes
4. **Chart optimization:** Memoize chart components
5. **Network throttling:** Test on 3G to ensure UX

---

## Summary Decision Matrix

| Need | Library | Bundle | Reason |
|------|---------|--------|--------|
| Timeline | **Recharts** | 70KB | Lightweight, simple, perfect fit |
| Heatmap | **ECharts** | 200KB | Best heatmap support |
| Network | **vis-network** | 250KB | Force-directed, interactive |
| Table | **TanStack Table** | 8KB | Already installed, headless |

**Total new: 528 KB** (acceptable for rich analytics)

Ready to implement? Coordinate BE on endpoints and we'll start Phase 1! 🚀
