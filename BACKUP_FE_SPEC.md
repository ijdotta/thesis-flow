# Backup & Restore Feature - Frontend Technical Specification

**Document Version:** 1.0  
**Created:** October 30, 2025  
**Status:** Ready for Implementation  
**Target Audience:** Frontend Engineers

---

## Table of Contents

1. [Overview](#overview)
2. [Feature Requirements](#feature-requirements)
3. [API Specification](#api-specification)
4. [User Flows](#user-flows)
5. [Implementation Guidelines](#implementation-guidelines)
6. [Security Considerations](#security-considerations)
7. [Error Handling](#error-handling)
8. [Testing Checklist](#testing-checklist)

---

## Overview

The Backup & Restore feature allows ADMIN users to export and import the entire system database. This is useful for:
- Creating snapshots of system state
- Migrating data between environments
- Disaster recovery
- Testing purposes

**Critical Security Requirement:** Restoring a backup completely wipes existing data. The UI must implement protective measures:
1. Automatic backup creation before restore
2. Password confirmation OR puzzle solve challenge
3. Clear warnings about data loss

---

## Feature Requirements

### 1. Create Backup

**Purpose:** Export all database tables to a JSON file for download.

**User Action:**
- ADMIN user clicks "Create Backup" button
- System exports all data to JSON
- File downloads automatically as `thesis-flow-backup-YYYY-MM-DD_HH-MM-SS.json`

**Requirements:**
- ✅ Button disabled/loading state during export
- ✅ Success notification with file name
- ✅ Show export timestamp
- ✅ Handle export errors gracefully

### 2. Restore Backup

**Purpose:** Import a previously created backup JSON file and restore system data.

**User Journey (HIGH PRIORITY - EXACT ORDER MATTERS):**

1. **User initiates restore** → clicks "Restore Backup" button
2. **Warning modal appears** showing:
   - "⚠️ This action will DELETE all current data"
   - "All existing data will be permanently removed and replaced"
   - "Current data cannot be recovered"
   - "Continue?" (Yes/No buttons)

3. **If Yes → Automatic Backup Creation**
   - System creates backup of current data BEFORE proceeding
   - Display progress indicator
   - Show backup file name and download link
   - Button: "I've saved the backup, proceed to restore"

4. **Security Challenge - User must complete ONE of:**
   - **Option A: Password Confirmation**
     - User enters their login password
     - System validates against current session
   - **Option B: Puzzle/Math Challenge** (fallback)
     - Simple math problem: e.g., "What is 7 + 5?" or "What is 12 ÷ 3?"
     - User solves and enters answer
     - System validates

5. **File Upload**
   - User selects restore file (JSON format)
   - Validate file format before sending

6. **Final Confirmation**
   - Show summary: "About to restore backup with X records"
   - "This is your last chance to cancel"
   - Button: "Restore" vs "Cancel"

7. **Execute Restore**
   - Show progress indicator
   - Disable UI during operation
   - Display completion status

---

## API Specification

### Endpoint 1: Create Backup

```http
GET /api/backup/create
Authorization: Bearer {TOKEN}
```

**Authentication:** Requires ADMIN role

**Response Headers:**
```
Content-Type: application/json
Content-Disposition: attachment; filename=thesis-flow-backup.json
```

**Response Body:** JSON object with structure:
```json
{
  "career": [
    { "id": 1, "publicId": "uuid-1", "name": "Computer Science", ... },
    { "id": 2, "publicId": "uuid-2", "name": "Engineering", ... }
  ],
  "person": [ ... ],
  "professor": [ ... ],
  "student": [ ... ],
  "student_career": [ ... ],
  "auth_user": [ ... ],
  "professor_login_token": [ ... ],
  "application_domain": [ ... ],
  "tag": [ ... ],
  "project": [ ... ],
  "project_participant": [ ... ]
}
```

**HTTP Status Codes:**
- `200 OK` - Backup created successfully
- `401 Unauthorized` - Missing/invalid token
- `403 Forbidden` - User doesn't have ADMIN role
- `500 Internal Server Error` - Server error during backup

**Example Usage:**
```javascript
const response = await fetch('/api/backup/create', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`,
  }
});

const blob = await response.blob();
const url = window.URL.createObjectURL(blob);
const a = document.createElement('a');
a.href = url;
a.download = `thesis-flow-backup-${new Date().toISOString().split('T')[0]}.json`;
a.click();
```

---

### Endpoint 2: Restore Backup

```http
POST /api/backup/restore
Content-Type: application/json
Authorization: Bearer {TOKEN}

{backup JSON content}
```

**Authentication:** Requires ADMIN role

**Request Body:** Complete JSON backup (same format as create endpoint response)

**Response:**
```json
{
  "message": "Backup restored successfully"
}
```

**HTTP Status Codes:**
- `200 OK` - Backup restored successfully
- `400 Bad Request` - Invalid JSON format
- `401 Unauthorized` - Missing/invalid token
- `403 Forbidden` - User doesn't have ADMIN role
- `500 Internal Server Error` - Server error during restore

**Example Usage:**
```javascript
const response = await fetch('/api/backup/restore', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  },
  body: backupJsonContent // string
});

if (response.ok) {
  const data = await response.json();
  console.log(data.message); // "Backup restored successfully"
} else {
  const error = await response.json();
  console.error('Restore failed:', error);
}
```

---

## User Flows

### Flow 1: Create Backup

```
User (ADMIN)
    ↓
[Click "Create Backup" button]
    ↓
[Button shows "Loading..." state]
    ↓
GET /api/backup/create
    ↓
[System returns JSON file]
    ↓
[File downloads: thesis-flow-backup-2025-10-30.json]
    ↓
[Success toast: "Backup created successfully"]
    ↓
[User can access file in downloads folder]
```

### Flow 2: Restore Backup (CRITICAL - MUST FOLLOW EXACTLY)

```
User (ADMIN)
    ↓
[Click "Restore Backup" button]
    ↓
[Warning Modal appears]
   "⚠️ This will DELETE all current data"
   [Cancel] [Yes, continue]
    ↓
[User clicks "Yes"]
    ↓
[Automatic Backup Creation]
GET /api/backup/create
    ↓
[Current data backed up]
[Show: "Pre-restore backup created: thesis-flow-backup-safe-2025-10-30_143022.json"]
[Show download link for safety backup]
    ↓
[User clicks "I've saved the backup, proceed"]
    ↓
[Security Challenge Modal]
   Option A: "Enter your password"
   Option B: "Solve: 15 ÷ 3 = ?"
    ↓
[User completes challenge]
    ↓
[File Upload Input]
[User selects: thesis-flow-backup-2025-10-28.json]
    ↓
[Validation: Check JSON format]
    ↓
[If valid:]
[Final Confirmation Modal]
   "Ready to restore backup with 250+ records"
   "This action is irreversible"
   [Cancel] [Restore]
    ↓
[If Cancel: return to main backup screen]
    ↓
[If Restore:]
POST /api/backup/restore {backup data}
    ↓
[Show progress indicator: "Restoring... 0-100%"]
    ↓
[System completes restore]
    ↓
[Success: "Restore completed successfully"]
[Info: "System will refresh in 3 seconds..."]
    ↓
[Auto-refresh page / redirect to dashboard]
```

---

## Implementation Guidelines

### UI Components to Create

1. **BackupRestoreAdmin** (Main Container)
   - Container for backup/restore functionality
   - Only visible to ADMIN users
   - Location: Settings / Admin Panel

2. **CreateBackupButton**
   - Button with loading state
   - Downloads file on success
   - Shows success/error toast

3. **WarningModal**
   - First step of restore flow
   - Clear danger messaging
   - Two buttons: Cancel, Yes continue

4. **AutoBackupStatus**
   - Shows progress of pre-restore backup
   - Displays backup file info
   - Provides download option for safety

5. **SecurityChallengeModal**
   - Tabs/Radio buttons for two options:
     - Password input (masked)
     - Math problem display with input field
   - Validate input before enabling proceed

6. **FileUploadInput**
   - Drag-and-drop zone
   - File picker
   - File validation (JSON format check)
   - Show file name/size

7. **FinalConfirmationModal**
   - Display summary of what will happen
   - Show record counts from backup if possible
   - Buttons: Cancel, Restore

### State Management

```typescript
interface BackupRestoreState {
  // Create Backup
  isCreatingBackup: boolean;
  createBackupError: string | null;
  
  // Restore Backup
  isRestoringBackup: boolean;
  restoreError: string | null;
  
  // Flow state
  restoreStep: 'idle' | 'warning' | 'auto-backup' | 'security-challenge' 
              | 'file-upload' | 'final-confirm' | 'restoring' | 'complete';
  
  // Pre-restore backup
  preRestoreBackupFile: string | null;
  preRestoreBackupCreated: boolean;
  
  // Selected restore file
  selectedRestoreFile: File | null;
  selectedRestoreData: string | null;
  
  // Security
  securityChallengeType: 'password' | 'puzzle';
  mathProblem: { question: string; answer: number };
}
```

### Error Handling

```typescript
interface BackupError {
  code: 'INVALID_JSON' | 'NETWORK_ERROR' | 'AUTH_ERROR' | 'SERVER_ERROR';
  message: string;
  userMessage: string;
}

// Map to user-friendly messages:
{
  'INVALID_JSON': 'The selected file is not a valid backup. Please choose a correct file.',
  'NETWORK_ERROR': 'Network error. Please check your connection and try again.',
  'AUTH_ERROR': 'You do not have permission to restore backups. Contact an administrator.',
  'SERVER_ERROR': 'Server error during restore. Please try again or contact support.'
}
```

---

## Security Considerations

### 1. Password Confirmation (Recommended)

**Implementation:**
- User enters password
- Validate against current session token (don't send to backend)
- Could also use backend validation if needed for audit

**Validation Options:**
```javascript
// Option A: Frontend only (simpler)
const userPassword = passwordInput.value;
const isValid = await validatePassword(userPassword, currentUser.id);

// Option B: Backend validation (more secure)
const response = await fetch('/api/auth/verify-password', {
  method: 'POST',
  headers: { 'Authorization': `Bearer ${token}` },
  body: JSON.stringify({ password: userPassword })
});
```

### 2. Puzzle/Math Challenge (Fallback)

If password validation not available:
```javascript
// Simple math challenges
const challenges = [
  { q: 'What is 7 + 5?', a: 12 },
  { q: 'What is 15 ÷ 3?', a: 5 },
  { q: 'What is 3 × 4?', a: 12 },
  { q: 'What is 20 - 8?', a: 12 },
  { q: 'What is 6 + 7?', a: 13 }
];

const random = challenges[Math.floor(Math.random() * challenges.length)];
```

### 3. Pre-Restore Backup

**CRITICAL:** Always create backup before restore
```javascript
// Step 1: Create current backup
const currentBackup = await fetch('/api/backup/create', {...});
const blob = await currentBackup.blob();

// Step 2: User confirms they've saved it
// Step 3: Only then proceed with restore
```

### 4. File Validation

```javascript
// Validate JSON structure
function validateBackupFile(jsonData) {
  const requiredTables = [
    'career', 'person', 'professor', 'student', 'student_career',
    'auth_user', 'professor_login_token', 'application_domain', 
    'tag', 'project', 'project_participant'
  ];
  
  for (const table of requiredTables) {
    if (!Array.isArray(jsonData[table])) {
      throw new Error(`Invalid backup: missing or invalid ${table} table`);
    }
  }
  
  return true;
}
```

### 5. Session Security

- Ensure user is still authenticated when clicking restore
- Check token validity before each API call
- Show warning if session about to expire
- Redirect to login if auth fails mid-flow

---

## Error Handling

### Network Errors

```javascript
try {
  const response = await fetch('/api/backup/create', {
    headers: { 'Authorization': `Bearer ${token}` }
  });
} catch (error) {
  if (error instanceof TypeError) {
    showError('Network connection error. Please check your internet.');
  }
}
```

### Invalid JSON

```javascript
// When user selects file
const file = event.target.files[0];
const text = await file.text();

try {
  const json = JSON.parse(text);
  validateBackupFile(json);
} catch (error) {
  showError('Invalid backup file. Please select a valid thesis-flow backup.');
}
```

### Auth Errors

```javascript
if (response.status === 401) {
  showError('Session expired. Please log in again.');
  redirectToLogin();
} else if (response.status === 403) {
  showError('Only administrators can restore backups.');
}
```

### Server Errors

```javascript
if (response.status === 500) {
  showError('Server error during backup restore. Please try again later.');
  // Log to monitoring service
}
```

---

## Testing Checklist

### Unit Tests
- [ ] Backup file validation logic
- [ ] Math puzzle generation and validation
- [ ] State transitions through restore flow
- [ ] Error message mapping

### Integration Tests
- [ ] Create backup API call
- [ ] File download functionality
- [ ] Restore backup API call with valid data
- [ ] Pre-restore backup creation
- [ ] Page refresh after successful restore

### E2E Tests
- [ ] Complete backup creation flow
- [ ] Complete restore flow with password
- [ ] Complete restore flow with puzzle
- [ ] Restore flow cancellation at each step
- [ ] Error handling (invalid file, network error, auth error)
- [ ] Pre-restore backup functionality

### Security Tests
- [ ] Password is masked in input
- [ ] Password validation works correctly
- [ ] Math puzzle validates correct answer
- [ ] Invalid answers block restore
- [ ] Pre-restore backup is created before restore starts
- [ ] Unauthorized users cannot access feature

### Edge Cases
- [ ] Very large backup files
- [ ] Corrupted JSON files
- [ ] Network timeout during restore
- [ ] User closes browser during restore
- [ ] Multiple restore attempts
- [ ] Session expires during flow

---

## Important Notes for Frontend

1. **Flow is Sequential** - Users must follow the exact flow: warning → auto-backup → security challenge → file upload → final confirm → restore

2. **No Shortcuts** - Don't allow skipping the pre-restore backup or security challenge

3. **Clear Warnings** - Use visual indicators (icons, colors, bold text) to emphasize data loss

4. **Save Pre-Backup** - Automatically provide download link for safety backup before proceeding

5. **Progress Indication** - Show progress during long operations (backup creation, restore process)

6. **Auto-Refresh** - After successful restore, refresh the page or redirect to dashboard (data has changed)

7. **Role-Based Access** - Only show this feature to ADMIN users

8. **Audit Trail** - Consider logging restore attempts (successful and failed) if audit feature exists

---

## Deployment Checklist

- [ ] Feature flag enabled for ADMIN users
- [ ] UI tested in all target browsers
- [ ] Mobile responsiveness tested (if applicable)
- [ ] Error messages reviewed with product team
- [ ] Security review completed
- [ ] Performance tested with large backups
- [ ] Documentation updated
- [ ] User training/notification planned

---

## References

- Backend API Documentation: See `BACKUP.md`
- Security Considerations: See backend implementation notes
- Related: Database Schema, Auth System

---

**Questions or Issues?** Contact Backend Team or Project Lead.
