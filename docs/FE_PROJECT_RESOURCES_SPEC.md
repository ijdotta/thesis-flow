# Frontend Feature Specification: Project Resources Management

## Overview

This feature allows users to add, view, edit, and delete external resources (files, links) associated with a project. Resources include links to external files hosted on GitHub, Google Drive, or other platforms. Each resource contains:
- **URL**: Link to the external resource
- **Title**: File name or descriptive title
- **Description**: Optional details about the resource

This feature enriches projects with supplementary materials without storing files directly in the system.

---

## API Endpoints

### Update Project Resources
```
PUT /api/projects/{projectId}/resources
```

#### Request

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <token>
```

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| projectId | string (UUID) | The public ID of the project |

**Request Body:**
```json
[
  {
    "url": "https://github.com/username/repo/blob/main/thesis.pdf",
    "title": "Thesis PDF",
    "description": "Final thesis document with complete research"
  },
  {
    "url": "https://drive.google.com/file/d/1234567890/view",
    "title": "Research Data",
    "description": "Supporting research data"
  }
]
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| url | string | Yes | Valid HTTP/HTTPS URL to external resource |
| title | string | Yes | File name or descriptive title (max 255 characters, non-empty) |
| description | string | No | Optional details about the resource (max 1000 characters) |

**Note**: The array represents the complete list of resources for the project. Send an empty array `[]` to remove all resources.

#### Response

**Success (200 OK):**
```json
{
  "publicId": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Advanced Machine Learning Framework",
  "type": "THESIS",
  "subtype": ["RESEARCH", "IMPLEMENTATION"],
  "initialSubmission": "2025-03-15",
  "completion": "2025-12-31",
  "careerPublicId": "550e8400-e29b-41d4-a716-446655440001",
  "career": {
    "publicId": "550e8400-e29b-41d4-a716-446655440001",
    "name": "Computer Science"
  },
  "applicationDomainDTO": {
    "publicId": "550e8400-e29b-41d4-a716-446655440002",
    "name": "Machine Learning"
  },
  "tags": [
    {
      "publicId": "550e8400-e29b-41d4-a716-446655440003",
      "name": "AI"
    }
  ],
  "participants": [
    {
      "personPublicId": "550e8400-e29b-41d4-a716-446655440004",
      "name": "Dr. Jane Smith",
      "email": "jane.smith@example.com",
      "role": "DIRECTOR"
    }
  ],
  "resources": [
    {
      "url": "https://github.com/username/repo/blob/main/thesis.pdf",
      "title": "Thesis PDF",
      "description": "Final thesis document with complete research"
    },
    {
      "url": "https://drive.google.com/file/d/1234567890/view",
      "title": "Research Data",
      "description": "Supporting research data"
    }
  ]
}
```

**Error (400 Bad Request):**
- Invalid URL format in any resource
- Missing required fields in any resource
- Title or description exceeds max length in any resource

**Error (401 Unauthorized):**
- Missing or invalid authentication token

**Error (403 Forbidden):**
- User does not have permission to modify this project (only ADMIN or project director/co-director)

**Error (404 Not Found):**
- Project with the given ID does not exist

---

## Frontend Implementation Guide

### 1. Data Model

Define TypeScript interfaces:

```typescript
interface ProjectResource {
  url: string;
  title: string;
  description?: string;
}

interface ProjectResourceRequest {
  url: string;
  title: string;
  description?: string;
}

interface ProjectDTO {
  // ... existing fields
  resources?: ProjectResource[];
}
```

### 2. API Service Layer

```typescript
class ProjectResourceService {
  async updateResources(
    projectId: string,
    resources: ProjectResourceRequest[]
  ): Promise<ProjectDTO> {
    const response = await fetch(
      `/api/projects/${projectId}/resources`,
      {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${getAuthToken()}`
        },
        body: JSON.stringify(resources)
      }
    );

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to update resources');
    }

    return response.json();
  }
}
```

### 3. UI Components

#### Main Resources List Component
```
ProjectResourcesPanel
├── ResourcesList
│   ├── ResourceItem (for each resource)
│   │   ├── ResourceTitle
│   │   ├── ResourceLink
│   │   ├── ResourceDescription
│   │   ├── EditButton
│   │   └── DeleteButton
│   └── EmptyState (when no resources)
├── AddResourceButton
└── ResourceForm (modal or inline)
    ├── URLInput
    ├── TitleInput
    ├── DescriptionInput
    ├── SaveButton
    └── CancelButton
```

#### 3.1 Resources List Component

```jsx
interface ProjectResourcesListProps {
  projectId: string;
  resources: ProjectResource[];
  canEdit: boolean;
  onResourcesChanged: (updatedResources: ProjectResource[]) => void;
}

export function ProjectResourcesList({
  projectId,
  resources,
  canEdit,
  onResourcesChanged
}: ProjectResourcesListProps) {
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingIndex, setEditingIndex] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!canEdit) {
    // Read-only mode
    return (
      <div className="resources-section">
        <h3>Project Resources</h3>
        {resources && resources.length > 0 ? (
          <ul className="resources-list">
            {resources.map((resource, index) => (
              <ResourceItem key={index} resource={resource} />
            ))}
          </ul>
        ) : (
          <p className="empty-state">No resources added yet</p>
        )}
      </div>
    );
  }

  return (
    <div className="resources-section">
      <div className="resources-header">
        <h3>Project Resources</h3>
        {!isFormOpen && editingIndex === null && (
          <button
            onClick={() => setIsFormOpen(true)}
            className="btn btn-primary"
          >
            + Add Resource
          </button>
        )}
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {resources && resources.length > 0 ? (
        <ul className="resources-list">
          {resources.map((resource, index) => (
            <ResourceItem
              key={index}
              resource={resource}
              index={index}
              isEditing={editingIndex === index}
              onEdit={() => setEditingIndex(index)}
              onDelete={() => handleDeleteResource(index)}
            />
          ))}
        </ul>
      ) : (
        <p className="empty-state">No resources added yet</p>
      )}

      {isFormOpen && (
        <ResourceForm
          onSubmit={(resource) => handleAddResource(resource)}
          onCancel={() => {
            setIsFormOpen(false);
            setError(null);
          }}
          isLoading={loading}
        />
      )}

      {editingIndex !== null && (
        <ResourceForm
          initialValues={resources[editingIndex]}
          onSubmit={(resource) => handleUpdateResource(editingIndex, resource)}
          onCancel={() => {
            setEditingIndex(null);
            setError(null);
          }}
          isLoading={loading}
          isEditing={true}
        />
      )}
    </div>
  );

  async function handleAddResource(resource: ProjectResourceRequest) {
    setLoading(true);
    setError(null);
    try {
      const newResources = [...resources, resource];
      const updatedProject = await projectResourceService.updateResources(
        projectId,
        newResources
      );
      onResourcesChanged(updatedProject.resources || []);
      setIsFormOpen(false);
      // Show success toast
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleUpdateResource(
    index: number,
    resource: ProjectResourceRequest
  ) {
    setLoading(true);
    setError(null);
    try {
      const newResources = resources.map((r, i) => i === index ? resource : r);
      const updatedProject = await projectResourceService.updateResources(
        projectId,
        newResources
      );
      onResourcesChanged(updatedProject.resources || []);
      setEditingIndex(null);
      // Show success toast
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleDeleteResource(index: number) {
    if (!confirm('Are you sure you want to delete this resource?')) {
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const newResources = resources.filter((_, i) => i !== index);
      const updatedProject = await projectResourceService.updateResources(
        projectId,
        newResources
      );
      onResourcesChanged(updatedProject.resources || []);
      // Show success toast
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }
}
```

#### 3.2 Individual Resource Item Component

```jsx
interface ResourceItemProps {
  resource: ProjectResource;
  index?: number;
  isEditing?: boolean;
  onEdit?: () => void;
  onDelete?: () => void;
}

export function ResourceItem({
  resource,
  index,
  isEditing,
  onEdit,
  onDelete
}: ResourceItemProps) {
  const openInNewTab = (url: string) => {
    window.open(url, '_blank', 'noopener,noreferrer');
  };

  return (
    <li className="resource-item">
      <div className="resource-content">
        <div className="resource-header">
          <h4 className="resource-title">{resource.title}</h4>
          <a
            href={resource.url}
            target="_blank"
            rel="noopener noreferrer"
            className="resource-link-button"
            title="Open in new tab"
          >
            <ExternalLinkIcon />
          </a>
        </div>

        {resource.description && (
          <p className="resource-description">{resource.description}</p>
        )}

        <div className="resource-url">
          <code>{truncateUrl(resource.url, 60)}</code>
        </div>
      </div>

      {onEdit && onDelete && (
        <div className="resource-actions">
          <button
            onClick={onEdit}
            className="btn btn-sm btn-secondary"
            disabled={isEditing}
          >
            Edit
          </button>
          <button
            onClick={onDelete}
            className="btn btn-sm btn-danger"
            disabled={isEditing}
          >
            Delete
          </button>
        </div>
      )}
    </li>
  );

  function truncateUrl(url: string, length: number): string {
    return url.length > length ? url.substring(0, length) + '...' : url;
  }
}
```

#### 3.3 Add/Edit Resource Form Component

```jsx
interface ResourceFormProps {
  initialValues?: ProjectResource;
  onSubmit: (resource: ProjectResourceRequest) => void;
  onCancel: () => void;
  isLoading: boolean;
  isEditing?: boolean;
}

export function ResourceForm({
  initialValues,
  onSubmit,
  onCancel,
  isLoading,
  isEditing = false
}: ResourceFormProps) {
  const [url, setUrl] = useState(initialValues?.url || '');
  const [title, setTitle] = useState(initialValues?.title || '');
  const [description, setDescription] = useState(initialValues?.description || '');
  const [errors, setErrors] = useState<Record<string, string>>({});

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!url.trim()) {
      newErrors.url = 'URL is required';
    } else if (!isValidUrl(url)) {
      newErrors.url = 'Must be a valid HTTP/HTTPS URL';
    }

    if (!title.trim()) {
      newErrors.title = 'Title is required';
    } else if (title.length > 255) {
      newErrors.title = 'Title cannot exceed 255 characters';
    }

    if (description.length > 1000) {
      newErrors.description = 'Description cannot exceed 1000 characters';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (validateForm()) {
      onSubmit({
        url: url.trim(),
        title: title.trim(),
        description: description.trim() || undefined
      });
    }
  };

  return (
    <div className="resource-form-container">
      <form onSubmit={handleSubmit} className="resource-form">
        <h4>{isEditing ? 'Edit Resource' : 'Add New Resource'}</h4>

        <div className="form-group">
          <label htmlFor="url">URL *</label>
          <input
            id="url"
            type="text"
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            placeholder="https://github.com/..."
            disabled={isLoading}
            className={errors.url ? 'error' : ''}
          />
          {errors.url && <span className="error-message">{errors.url}</span>}
        </div>

        <div className="form-group">
          <label htmlFor="title">Title *</label>
          <input
            id="title"
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="e.g., Thesis PDF, Research Data"
            maxLength={255}
            disabled={isLoading}
            className={errors.title ? 'error' : ''}
          />
          <span className="character-count">
            {title.length}/255
          </span>
          {errors.title && <span className="error-message">{errors.title}</span>}
        </div>

        <div className="form-group">
          <label htmlFor="description">Description</label>
          <textarea
            id="description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Optional: Add details about this resource"
            maxLength={1000}
            rows={3}
            disabled={isLoading}
            className={errors.description ? 'error' : ''}
          />
          <span className="character-count">
            {description.length}/1000
          </span>
          {errors.description && <span className="error-message">{errors.description}</span>}
        </div>

        <div className="form-actions">
          <button
            type="submit"
            disabled={isLoading}
            className="btn btn-primary"
          >
            {isLoading ? 'Saving...' : isEditing ? 'Update Resource' : 'Add Resource'}
          </button>
          <button
            type="button"
            onClick={onCancel}
            disabled={isLoading}
            className="btn btn-secondary"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );

  function isValidUrl(urlString: string): boolean {
    try {
      const url = new URL(urlString);
      return url.protocol === 'http:' || url.protocol === 'https:';
    } catch {
      return false;
    }
  }
}
```

### 4. UI Placement

Add the resources section to the project details page:

```jsx
<ProjectDetailsPage projectId={projectId}>
  {/* Existing sections */}
  <ProjectBasicInfo project={project} />
  <ProjectParticipants project={project} />
  <ProjectTags project={project} />
  
  {/* New Resources Section */}
  <ProjectResourcesList
    projectId={projectId}
    resources={project.resources || []}
    canEdit={canModifyProject}
    onResourcesChanged={handleResourcesUpdated}
  />
</ProjectDetailsPage>
```

### 5. Authorization

- Display resources panel even for read-only users
- Show action buttons (Add, Edit, Delete) only if user can modify the project
- Authorization is enforced on the backend:
  - **ADMIN** role: Can manage resources for any project
  - **PROFESSOR** role: Can manage resources only for projects they own (as director/co-director)
- Display appropriate error message if user attempts unauthorized action

### 6. Validation

**Frontend Validation:**
- URL must be non-empty and valid HTTP/HTTPS format
- Title must be non-empty and ≤ 255 characters
- Description must be ≤ 1000 characters (optional)
- Show character counters for title and description
- Show real-time validation feedback

**Backend Validation:**
- URL format and protocol validation
- Field length validation
- Authorization check
- Project existence validation
- Resource index bounds checking

### 7. Error Handling

Display user-friendly error messages:
- "Invalid URL format" - for malformed URLs
- "Title is required" - for missing title
- "You don't have permission to modify this project" - for authorization failures
- "Project not found" - for missing project
- "Resource index out of bounds" - for invalid index
- "Failed to add resource: [error details]" - for generic failures

Show errors in:
- Inline form validation messages
- Alert banners above the resources list
- Toast notifications for success/failure

### 8. Loading States

- Show spinner/skeleton loader while resources are loading from API
- Disable form buttons during submission
- Show "Saving..." text on submit button during request
- Gray out resource items being edited

### 9. Empty State

When no resources are added:
```
"No resources added yet"
+ [Add Resource button]
```

### 10. Resource Display Features

- **Link Preview**: Show truncated URL with external link icon
- **Click to Open**: Make resource title clickable to open in new tab
- **Resource Count**: Display count of resources (e.g., "3 Resources")
- **Edit/Delete Actions**: Provide clear action buttons for each resource
- **Confirmation Dialog**: Ask for confirmation before deleting a resource

### 11. Styling Guidelines

```css
.resources-section {
  margin-top: 2rem;
  padding: 1.5rem;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background-color: #f9f9f9;
}

.resources-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.resources-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.resource-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 1rem;
  margin-bottom: 0.5rem;
  background-color: white;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
}

.resource-content {
  flex: 1;
}

.resource-title {
  margin: 0 0 0.25rem 0;
  color: #0066cc;
  text-decoration: none;
  cursor: pointer;
}

.resource-title:hover {
  text-decoration: underline;
}

.resource-url {
  font-size: 0.875rem;
  color: #666;
  margin-top: 0.5rem;
  word-break: break-all;
}

.resource-description {
  margin: 0.5rem 0 0 0;
  color: #555;
  font-size: 0.9rem;
}

.resource-actions {
  display: flex;
  gap: 0.5rem;
  margin-left: 1rem;
  white-space: nowrap;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-family: inherit;
}

.form-group input.error,
.form-group textarea.error {
  border-color: #dc3545;
  background-color: #fff5f5;
}

.error-message {
  display: block;
  color: #dc3545;
  font-size: 0.875rem;
  margin-top: 0.25rem;
}

.character-count {
  display: block;
  font-size: 0.75rem;
  color: #999;
  text-align: right;
  margin-top: 0.25rem;
}

.empty-state {
  padding: 1rem;
  text-align: center;
  color: #999;
}
```

### 12. Testing Checklist

- [ ] Resources panel displays correctly in project details
- [ ] Add Resource button opens form
- [ ] Form validates URL format (rejects invalid URLs)
- [ ] Form validates title (rejects empty, enforces max length)
- [ ] Form allows optional description with character count
- [ ] Adding valid resource submits to API correctly
- [ ] Resource appears in list after successful addition
- [ ] Edit button allows modifying existing resource
- [ ] Delete button removes resource with confirmation
- [ ] Loading spinner shows during API calls
- [ ] Success toast/notification displays after CRUD operations
- [ ] Error messages display when API returns errors
- [ ] Read-only users see resources but no action buttons
- [ ] Unauthorized users see appropriate error message
- [ ] Resource links open in new tab correctly
- [ ] Form cancellation closes without saving changes
- [ ] Multiple resources display properly in list
- [ ] Empty state displays when no resources exist
- [ ] Pagination (if many resources) works correctly
- [ ] Resources persist after page refresh
- [ ] Mobile responsiveness: form and list are readable on small screens
- [ ] Accessibility: form labels associated with inputs, keyboard navigation works

---

## Integration with Existing Features

### Fetch Full Project with Resources

When fetching a project, resources will be included in the response:

```typescript
async function getProject(projectId: string): Promise<ProjectDTO> {
  const response = await fetch(`/api/projects/${projectId}`);
  const project: ProjectDTO = await response.json();
  
  // Resources will be available as:
  const resources = project.resources; // ProjectResource[]
  
  return project;
}
```

### Project List View

Consider showing a resources count badge in project list items:

```jsx
<ProjectListItem project={project}>
  {project.resources && project.resources.length > 0 && (
    <badge className="resources-badge">
      📎 {project.resources.length}
    </badge>
  )}
</ProjectListItem>
```

---

## API Error Response Format

When an error occurs, the API returns:

```json
{
  "error": "error-code",
  "message": "Human-readable error message"
}
```

Example error responses:

**Invalid URL (400):**
```json
{
  "error": "INVALID_URL",
  "message": "URL must be a valid HTTP or HTTPS URL"
}
```

**Not Found (404):**
```json
{
  "error": "NOT_FOUND",
  "message": "Project or resource not found"
}
```

**Forbidden (403):**
```json
{
  "error": "FORBIDDEN",
  "message": "You don't have permission to modify this project"
}
```

---

## Performance Considerations

1. **Lazy Loading**: Load resources only when project details are viewed
2. **Pagination**: If a project has many resources (>50), consider paginating the resources list
3. **Debouncing**: Debounce character inputs in form to provide real-time feedback without overwhelming the UI
4. **Caching**: Cache the project object after fetching to reduce redundant API calls

---

## Future Enhancements

- **Resource Categories**: Tag resources by type (Code, Documentation, Data, etc.)
- **Resource Preview**: Show file previews for certain resource types
- **Bulk Upload**: Allow uploading multiple resources at once
- **Resource Sorting**: Sort resources by date added, title, or type
- **Resource Tagging**: Add custom tags to organize resources
- **Comments on Resources**: Allow team members to comment on resources

---

**Backend Feature Branch**: `feature/project-resources`  
**API Base URL**: `/api/projects`  
**Last Updated**: 2025-11-01
