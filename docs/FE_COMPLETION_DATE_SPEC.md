# Frontend Feature Specification: Set Project Completion Date

## Overview
This feature allows users to set or update the completion date for a project. The completion date marks when a project was finished or is expected to be finished.

## Endpoint

### Set Completion Date
```
PUT /api/projects/{projectId}/completion
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
{
  "completionDate": "YYYY-MM-DD"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| completionDate | LocalDate (ISO 8601 format) | Yes | The date when the project is/was completed. Format: `YYYY-MM-DD` (e.g., `2025-12-31`) |

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
  ]
}
```

**Error (400 Bad Request):**
Invalid date format or missing required fields.

**Error (401 Unauthorized):**
Missing or invalid authentication token.

**Error (403 Forbidden):**
User does not have permission to modify this project.

**Error (404 Not Found):**
Project with the given ID does not exist.

## Frontend Implementation Guide

### 1. UI Component

#### Create a completion date setter component with:
- **Input field**: Date picker (HTML5 `<input type="date">` or a date picker library)
- **Submit button**: "Save Completion Date" or "Mark Complete"
- **Loading state**: Show spinner/disabled button during API call
- **Success message**: Toast/notification confirming the update
- **Error handling**: Display error message to user

#### Suggested Component Structure:
```
ProjectCompletionDateSetter
├── DateInputField
├── SubmitButton
├── LoadingSpinner
└── ErrorMessage
```

### 2. API Integration

#### Example API call (JavaScript/TypeScript):
```typescript
async function setProjectCompletionDate(
  projectId: string, 
  completionDate: Date
): Promise<ProjectDTO> {
  const formattedDate = completionDate.toISOString().split('T')[0]; // YYYY-MM-DD
  
  const response = await fetch(
    `/api/projects/${projectId}/completion`,
    {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${getAuthToken()}`
      },
      body: JSON.stringify({
        completionDate: formattedDate
      })
    }
  );

  if (!response.ok) {
    throw new Error(`Failed to set completion date: ${response.statusText}`);
  }

  return response.json();
}
```

### 3. User Experience Flow

1. **View Project Details**: User navigates to a project page
2. **Locate Completion Date Section**: Show current completion date (if set) or placeholder
3. **Click Edit/Set Button**: Opens date picker or inline input
4. **Select Date**: User picks a date using date picker
5. **Submit**: User clicks confirm/save button
6. **Loading State**: Show loading indicator while API request is in progress
7. **Success**: 
   - Display success message
   - Update UI with new completion date
   - Close modal/inline editor
8. **Error**:
   - Display error message
   - Allow user to retry

### 4. Authorization
- Only authorized users (project directors, co-directors, or admins) can set the completion date
- Attempting to set the date without proper permissions will return a 403 Forbidden error
- Display appropriate error messages to user (e.g., "You don't have permission to modify this project")

### 5. Validation

**Frontend Validation:**
- Ensure a date is selected
- Ensure the date is not in an invalid format
- Optionally: Prevent selecting dates in the future (if business logic requires)
- Optionally: Prevent selecting dates before `initialSubmission` date

**Backend Validation:**
- Date format validation (ISO 8601)
- Authorization check
- Project existence validation

### 6. UI Placement Suggestions

**Option A: Dedicated Section**
- Add a "Project Timeline" or "Key Dates" section to the project details page
- Display both `initialSubmission` and `completion` dates
- Show edit buttons for each date field

**Option B: Quick Action in Header**
- Add a "Mark Complete" button in the project header
- Opens a modal with date picker

**Option C: Inline Edit**
- Display completion date on the project details
- Click to edit inline with a date picker

### 7. Date Display

Always display dates in a user-friendly format:
- **Backend returns**: `YYYY-MM-DD` (ISO 8601)
- **Frontend displays**: Formatted according to user's locale (e.g., "December 31, 2025" or "31/12/2025")

### 8. Related Fields

**Contextual Information to Display:**
- `initialSubmission`: The date the project was initially submitted
- `completion`: The date when the project was/will be completed
- Show the time elapsed between these dates if project is completed

### 9. Testing Checklist

- [ ] Date picker opens and closes correctly
- [ ] Valid dates can be selected and submitted
- [ ] Invalid dates are rejected by frontend validation
- [ ] Loading spinner shows during API request
- [ ] Success message displays after successful update
- [ ] Project details update with new completion date
- [ ] Error message displays if API returns error
- [ ] User without permission sees appropriate error
- [ ] Date format is correctly sent to backend (YYYY-MM-DD)
- [ ] Date is correctly displayed after refresh/reload

## Example Integration with Existing Components

If you have a project detail view, add the completion date setter:

```jsx
<ProjectDetailsPanel projectId={projectId}>
  {/* Existing fields */}
  <ProjectField label="Initial Submission" value={project.initialSubmission} />
  
  {/* New completion date section */}
  <ProjectCompletionDateSetter 
    projectId={projectId}
    currentCompletionDate={project.completion}
    onSuccessfulUpdate={(updatedProject) => {
      // Update local state or parent component
      setProject(updatedProject);
    }}
  />
</ProjectDetailsPanel>
```

## Status Indicator

Consider showing a completion status badge:
- **Not Completed**: Show "In Progress" badge, allow setting completion date
- **Completed**: Show completion date with checkmark, allow changing date
- **Show Days Until/Since**: Calculate and display days between submission and completion

---

**Backend Feature Branch**: `feature/set-completion-date`  
**API Base URL**: `/api/projects`  
**Last Updated**: 2025-10-28
