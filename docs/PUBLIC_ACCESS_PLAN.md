# Public Access View - Architecture & Library Planning

## Overview
New public-facing section for university to showcase thesis/project data without authentication. Two main areas:
1. **Browse Projects** - Searchable/filterable project directory
2. **Analytics Dashboard** - Visualizations of project trends, topics, collaborations

## Navigation Structure
```
/public
├── /public/projects        - Browse/search all projects
├── /public/analytics       - Analytics dashboard
│   ├── /public/analytics?view=timeline    (default)
│   ├── /public/analytics?view=topics
│   ├── /public/analytics?view=professors
│   └── /public/analytics?view=statistics
└── /public/project/:id     - Individual project detail (future: with resources)
```

## Features & Visualizations

### 1. Browse Projects Page
**Components:**
- Search bar (title, keywords)
- Filters sidebar:
  - Career (multi-select)
  - Professor/Director (autocomplete)
  - Date range slider (year)
  - Project type (THESIS/FINAL_PROJECT)
- Results: Grid or table view

**Data needed:**
- Project title, description, type
- Career info
- Participants with roles
- Dates
- Tags

---

### 2. Analytics Dashboard

#### Filter Panel (Applied to all charts)
- Career filter (multi-select)
- Professor filter (multi-select autocomplete)
- Date range (year picker or slider)
- Real-time filter application across all visualizations

#### Chart 1: Timeline - Thesis Per Professor Over Time
**Type:** Line chart with multiple series

**Requirements:**
- X-axis: Years (1-year resolution)
- Y-axis: Number of projects
- Series: One line per professor (top N professors, e.g., top 5)
- Stacked or multi-line option
- Legend with professor names
- Hover tooltip showing exact count

**Library Recommendation:** **Recharts**
- ✅ Built on React components
- ✅ Interactive tooltips & legends
- ✅ Easy multi-series line charts
- ✅ Responsive out of box
- ✅ Good TypeScript support
- ✅ Lightweight, good performance

---

#### Chart 2: Heatmap - Topics Popularity Over Time
**Type:** 2D heatmap/matrix

**Requirements:**
- X-axis: Years
- Y-axis: Topics (sorted by total count, top N, e.g., top 10)
- Colors: Intensity = count of projects with that topic in that year
- Interactive: Click/hover shows exact count
- Color scale: Low=light, High=dark (or custom gradient)

**Library Recommendation:** **Recharts (custom ScatterChart)** OR **Plotly React**
- ❌ Recharts doesn't have native heatmap
- ✅ Plotly has excellent heatmap support
- ✅ Cell-by-cell coloring and interactivity
- ✅ Professional appearance

**Alternative:** Custom component using CSS grid + color mapping (lightweight)

**Decision:** Use **Plotly.js** (via `react-plotly.js`)
- Industry standard for heatmaps
- Professional appearance
- Rich interactivity
- Good TypeScript support
- Can also support other chart types if needed

---

#### Chart 3: Relationship Graph - Professor Collaborations
**Type:** Interactive network graph

**Requirements:**
- **Nodes:** Professors
  - Size ∝ number of projects they're involved in
  - Color: Optional (by career, department, or static)
  - Label: Professor name
  
- **Edges:** Collaborations between professors
  - Width ∝ number of shared projects
  - Color: Optional (gradient by frequency)
  - Opacity: Weight visibility
  
- **Interaction:**
  - Drag to reposition nodes
  - Hover to highlight connected nodes
  - Click for details panel (optional)
  - Zoom/pan
  - Force-directed layout (physics simulation)

**Library Recommendation:** **Vis.js** OR **Cytoscape.js** OR **D3.js**

**Best Option: Vis.js** (`react-vis-network`)
- ✅ Force-directed physics simulation (automatic layout)
- ✅ Built-in interactions (drag, zoom, pan)
- ✅ Good TypeScript support
- ✅ Lightweight
- ✅ Good for thousands of nodes
- ✅ Easy node/edge styling (size, color, width)

**Alternative:** `cytoscape` (more graph theory focused, heavier)

**Decision:** Use **vis.js** (`vis-network` package)
- Perfect for this use case
- Minimal learning curve
- Great for professor network visualization
- Performance tested with large networks

---

#### Chart 4: Statistics Table - Projects Per Career Per Year
**Type:** Data table / pivot table

**Requirements:**
- Rows: Careers (alphabetical or by project count)
- Columns: Years (chronological)
- Cells: Count of projects
- Totals: Row sums (career total) & column sums (year total)
- Sortable columns
- Export to CSV (optional future feature)
- Conditional formatting: Cell color intensity based on count

**Library Recommendation:** Built-in (no heavy library needed)
- Use **shadcn/ui Table** (already available)
- CSS for conditional formatting (Tailwind)
- Optional: **TanStack Table (React Table)** if complex sorting/filtering needed

**Decision:** Extend existing shadcn/ui table with Tailwind conditional colors
- Already in project
- No extra dependencies
- Fast rendering
- Good TypeScript support

---

## Data Flow Architecture

```
┌─────────────────────────────────────┐
│   Public Filters                    │
│  - Career, Professor, DateRange     │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│   API Call                          │
│   GET /public/analytics/data?...    │
│   (filtered aggregated stats)       │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│   React Context / Zustand?          │
│   Share filtered data across charts │
└──────────────┬──────────────────────┘
               │
       ┌───────┼───────┬────────┬───────┐
       ▼       ▼       ▼        ▼       ▼
    Timeline Heatmap Network Stats  Browse
   (Recharts)(Plotly)(Vis.js)(Table)(Grid)
```

---

## Library Summary & Installation Plan

### Already Installed ✅
- `react`, `react-dom`
- `react-router-dom`
- `@tanstack/react-query` (perfect for fetching filtered data)
- `shadcn/ui` (table, cards, buttons)
- `tailwindcss` (styling)

### To Install 📦

```bash
npm install recharts echarts react-use-echarts cytoscape @tanstack/react-table
```

| Visualization | Library | Why This One |
|---|---|---|
| 📊 **Timeline** (Thesis/Professor) | `recharts` | Simple API, excellent for time-series, responsive, React-native components |
| 🔥 **Heatmap** (Topics over time) | `echarts` + `react-use-echarts` | Specialized heatmap component, beautiful interactive visualizations |
| 🕸️ **Network Graph** (Professor collaborations) | `cytoscape` | Force-directed layout, handles weighted edges elegantly, performant |
| 📋 **Stats Table** (Career/Year pivot) | `@tanstack/react-table` | Headless (use with shadcn/ui styling), lightweight, excellent TypeScript support |

**Why these over alternatives:**
- **Recharts** over ECharts for timeline: simpler API for this use case, already familiar pattern
- **ECharts** over Plotly for heatmap: better heatmap component, lighter bundle
- **Cytoscape** over Vis.js for network: more stable, better edge handling, excellent physics simulation
- **TanStack Table** over MUI DataGrid: headless (no bloat), integrates seamlessly with shadcn/ui

---

## Component Structure (Proposed)

```
src/pages/public/
├── BrowseProjectsPage.tsx          # Main browse page
│   ├── ProjectFilters.tsx           # Filter sidebar
│   └── ProjectGrid.tsx              # Results grid/table
│
└── AnalyticsDashboardPage.tsx       # Main analytics page
    ├── AnalyticsFilters.tsx          # Common filters (career, prof, date)
    ├── charts/
    │   ├── TimelineChart.tsx         # Recharts (professor thesis over time)
    │   ├── TopicsHeatmap.tsx         # ECharts (topics popularity)
    │   ├── ProfessorNetwork.tsx      # Cytoscape.js (collaboration graph)
    │   └── StatsTable.tsx            # TanStack Table (career/year pivot)
    └── AnalyticsContext.tsx          # Share filtered data state

src/api/
├── public.ts                        # Public API endpoints (no auth)
└── analytics.ts                     # Analytics data aggregation endpoints

src/hooks/
├── usePublicFilters.ts              # Filter state management
└── useAnalyticsData.ts              # Data fetching with filters
```

---

## Implementation Strategy

### Phase 1: Setup & Browse Page
- [ ] Create `/public` route structure
- [ ] Implement BrowseProjectsPage with filters
- [ ] Create public API endpoints
- [ ] Build ProjectGrid/Table component

### Phase 2: Analytics Skeleton
- [ ] Create AnalyticsDashboardPage
- [ ] Build AnalyticsFilters (shared across all charts)
- [ ] Setup AnalyticsContext for filter state
- [ ] Add route navigation between views

### Phase 3: Chart 1 - Timeline
- [ ] Install Recharts
- [ ] Create TimelineChart component
- [ ] Wire to analytics data
- [ ] Test with sample data

### Phase 4: Chart 2 - Heatmap
- [ ] Install ECharts (`echarts` + `react-use-echarts`)
- [ ] Create TopicsHeatmap component
- [ ] Wire to analytics data
- [ ] Style/customize appearance

### Phase 5: Chart 3 - Network Graph
- [ ] Install Cytoscape
- [ ] Create ProfessorNetwork component
- [ ] Implement force-directed layout
- [ ] Add interactions (hover, click, drag)

### Phase 6: Chart 4 - Statistics Table
- [ ] Create StatsTable component
- [ ] Add conditional formatting
- [ ] Wire to analytics data
- [ ] Add sortable columns

### Phase 7: Polish
- [ ] Responsive design
- [ ] Loading states
- [ ] Error handling
- [ ] Performance optimization (memoization)

---

## API Endpoints (Backend to provide)

**All endpoints:**
- No authentication required
- Public access (no Authorization header needed)
- Data is **pre-aggregated on backend**; frontend just renders

### 1. Browse Projects
```
GET /public/projects
  Query Parameters:
    ?careerId=uuid                    (optional, multi-select: ?careerId=uuid1&careerId=uuid2)
    &professorId=uuid                 (optional, multi-select)
    &startYear=2020                   (optional)
    &endYear=2025                     (optional)
    &search=keyword                   (optional, searches title/tags/topics)
    &limit=20                         (optional, default 20)
    &offset=0                         (optional, pagination)
  
  Response:
  {
    "total": 150,
    "limit": 20,
    "offset": 0,
    "projects": [
      {
        "publicId": "uuid",
        "title": "Project Title",
        "type": "THESIS" | "FINAL_PROJECT",
        "initialSubmission": "2020-06-14",
        "completion": "2020-09-14",
        "career": {
          "publicId": "uuid",
          "name": "Carrera"
        },
        "applicationDomain": {
          "publicId": "uuid",
          "name": "Domain Name"
        },
        "tags": [
          { "publicId": "uuid", "name": "Tag Name" }
        ],
        "participants": [
          {
            "role": "DIRECTOR" | "CO_DIRECTOR" | "COLLABORATOR" | "STUDENT",
            "person": {
              "publicId": "uuid",
              "name": "Juan",
              "lastname": "Pérez"
            }
          }
        ]
      }
    ]
  }
```

### 2. Timeline Chart - Thesis Per Professor Over Time
```
GET /public/analytics/timeline
  Query Parameters:
    ?careerId=uuid                    (optional, multi-select)
    &professorId=uuid                 (optional, multi-select)
    &startYear=2020                   (optional)
    &endYear=2025                     (optional)
  
  Response (data already aggregated):
  {
    "data": [
      {
        "year": 2020,
        "professors": [
          {
            "professorId": "uuid",
            "professorName": "Dr. Juan Pérez",
            "projectCount": 5
          },
          {
            "professorId": "uuid",
            "professorName": "Dr. María García",
            "projectCount": 3
          }
        ]
      },
      {
        "year": 2021,
        "professors": [...]
      }
    ]
  }
```

### 3. Heatmap - Topics Popularity Over Time
```
GET /public/analytics/topics-heatmap
  Query Parameters:
    ?careerId=uuid                    (optional, multi-select)
    &professorId=uuid                 (optional, multi-select)
    &startYear=2020                   (optional)
    &endYear=2025                     (optional)
  
  Response (pre-aggregated matrix):
  {
    "data": [
      {
        "year": 2020,
        "topics": [
          {
            "topicId": "uuid",
            "topicName": "Artificial Intelligence",
            "count": 5
          },
          {
            "topicId": "uuid",
            "topicName": "Blockchain",
            "count": 2
          }
        ]
      },
      {
        "year": 2021,
        "topics": [...]
      }
    ]
  }
```

### 4. Network Graph - Professor Collaborations
```
GET /public/analytics/professor-network
  Query Parameters:
    ?careerId=uuid                    (optional, multi-select)
    &professorId=uuid                 (optional, multi-select; filters which profs to show)
    &startYear=2020                   (optional)
    &endYear=2025                     (optional)
  
  Response (nodes & edges):
  {
    "nodes": [
      {
        "id": "uuid",
        "label": "Dr. Juan Pérez",
        "size": 15,
        "projectCount": 25
      },
      {
        "id": "uuid",
        "label": "Dr. María García",
        "size": 12,
        "projectCount": 20
      }
    ],
    "edges": [
      {
        "from": "uuid",
        "to": "uuid",
        "weight": 3,
        "collaborationCount": 3
      },
      {
        "from": "uuid",
        "to": "uuid",
        "weight": 5,
        "collaborationCount": 5
      }
    ]
  }
```

### 5. Statistics Table - Projects Per Career Per Year
```
GET /public/analytics/stats-table
  Query Parameters:
    ?careerId=uuid                    (optional, multi-select)
    &professorId=uuid                 (optional, multi-select)
    &startYear=2020                   (optional)
    &endYear=2025                     (optional)
  
  Response (pivot table data):
  {
    "careerStats": [
      {
        "careerId": "uuid",
        "careerName": "Computer Science",
        "yearlyData": [
          { "year": 2020, "projectCount": 10 },
          { "year": 2021, "projectCount": 12 },
          { "year": 2022, "projectCount": 8 }
        ],
        "totalProjects": 30
      },
      {
        "careerId": "uuid",
        "careerName": "Engineering",
        "yearlyData": [...]
      }
    ],
    "years": [2020, 2021, 2022, 2023],
    "grandTotal": 150
  }
```

### 6. Project Detail (Future)
```
GET /public/project/:publicId
  Response:
  {
    "publicId": "uuid",
    "title": "Project Title",
    "type": "THESIS" | "FINAL_PROJECT",
    "description": "Optional description",
    "initialSubmission": "2020-06-14",
    "completion": "2020-09-14",
    "career": { ... },
    "applicationDomain": { ... },
    "tags": [ ... ],
    "participants": [ ... ],
    "resources": [
      {
        "resourceId": "uuid",
        "title": "Resource Title",
        "type": "GITHUB_REPO" | "DOCUMENT" | "DEMO_VIDEO" | "OTHER",
        "url": "https://...",
        "description": "Optional description"
      }
    ]
  }
```

---

## Notes & Considerations

1. **Caching Strategy:** Use TanStack Query with aggressive caching since public data changes less frequently
2. **Performance:** Network graph may need virtualization if 100+ professors; start without, optimize later
3. **Accessibility:** Ensure charts are WCAG 2.1 AA compliant (alt text, keyboard nav, color contrast)
4. **Mobile:** Charts should be responsive; consider simpler fallbacks on mobile
5. **No Authentication:** Public routes should not require login, visible from `/public/*`
6. **SEO Future:** Consider if these pages need SEO optimization (meta tags, sitemap)
7. **Future Resources:** Browse page prepared for future "project resources" feature (docs, repos, etc.)

---

## Decision Summary

| Visualization | Library | Reason |
|---|---|---|
| Timeline Line Chart | **Recharts** | Lightweight, React-native, perfect for multi-series |
| Topics Heatmap | **Plotly.js** | Industry standard, professional, native heatmap support |
| Professor Network | **vis-network** | Force-directed layout, interactivity, built for networks |
| Statistics Table | **shadcn/ui** | Already available, no need for heavy table library |

**Total new packages:** 4 (`recharts`, `plotly.js`, `react-plotly.js`, `vis-network`)
**No conflicts** with existing stack (React 19, TypeScript, Tailwind, Radix UI)
