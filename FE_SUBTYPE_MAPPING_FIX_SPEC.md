# Frontend SubType Mapping Fix - Specification

## Problem
The backend removed `TYPE_1` and `TYPE_2` placeholder enum values and now only supports:
- `INVESTIGACION`
- `VINCULACION`
- `EXTENSION`

However, the frontend is currently:
1. ✅ Displaying Spanish labels: 'Investigación', 'Extensión', 'Vinculación' (CORRECT)
2. ✅ Storing Spanish labels in `draft.subtypes` (CORRECT for display)
3. ❌ **Sending Spanish labels directly to the API when creating projects** (WRONG)

The API now expects the uppercase enum values, not Spanish labels.

## Root Cause
In `src/components/projectWizard/actions.ts`, the `createProject` call sends `draft.subtypes` directly without converting from Spanish labels to enum values.

Currently:
```typescript
const project = await createProject({
  // ... other fields
  subtypes: draft.subtypes,  // ❌ Sending: ['Investigación', 'Extensión', 'Vinculación']
});
```

Should be:
```typescript
const project = await createProject({
  // ... other fields
  subtypes: draft.subtypes.map(mapSpanishSubtypeToEnum),  // ✅ Sending: ['INVESTIGACION', 'EXTENSION', 'VINCULACION']
});
```

## Current State

### Frontend Enum Values (User Facing - Spanish)
- `SUBTYPE_OPTIONS` in `src/components/projectWizard/types.ts`:
  ```typescript
  export const SUBTYPE_OPTIONS = ['Investigación', 'Extensión', 'Vinculación'];
  ```

### Backend Enum Values (API Facing - English)
- Backend now only supports:
  - `INVESTIGACION`
  - `VINCULACION`
  - `EXTENSION`

### Mapper (Response Only)
- `src/mapper/responseToProjectMapper.ts` has `mapSubTypeToString()` that converts from Spanish to enum values
- This is only used when **receiving** data from the API
- **NOT used when sending data to the API**

## Solution Required

### 1. Create Mapping Function
Add a mapping function to convert Spanish labels to backend enum values.

**File**: `src/components/projectWizard/types.ts` or create `src/mappers/subtypeMapper.ts`

```typescript
// Mapping from Spanish display labels to backend enum values
const SPANISH_TO_ENUM_MAPPING: Record<string, string> = {
  'Investigación': 'INVESTIGACION',
  'Extensión': 'EXTENSION',
  'Vinculación': 'VINCULACION',
};

export const mapSpanishSubtypeToEnum = (spanishLabel: string): string => {
  return SPANISH_TO_ENUM_MAPPING[spanishLabel] || '';
};

export const mapSpanishSubtypesToEnum = (spanishLabels: string[]): string[] => {
  return spanishLabels
    .map(label => mapSpanishSubtypeToEnum(label))
    .filter(value => value !== '');
};
```

### 2. Update Project Wizard Actions
**File**: `src/components/projectWizard/actions.ts`

Change the `createProject` call:

```typescript
// BEFORE:
const project = await createProject({
  title: draft.title,
  type: draft.type,
  subtypes: draft.subtypes,  // ❌ Wrong: sending Spanish labels
  careerPublicId: draft.career.publicId,
  initialSubmission: draft.initialSubmission || undefined,
});

// AFTER:
import { mapSpanishSubtypesToEnum } from '@/mappers/subtypeMapper'; // or from types.ts

const project = await createProject({
  title: draft.title,
  type: draft.type,
  subtypes: mapSpanishSubtypesToEnum(draft.subtypes),  // ✅ Convert to enum values
  careerPublicId: draft.career.publicId,
  initialSubmission: draft.initialSubmission || undefined,
});
```

### 3. Update Import/Bulk API Calls (if applicable)
**File**: `src/components/projectWizard/steps/ImportStep.tsx` (if project import exists)

If there's an import feature that sends subtypes, apply the same mapping.

Also check:
- `src/api/import.ts` - if bulk import sends subtypes
- `src/api/publicApi.ts` - if public API sends subtypes

Any place where `subtypes` are sent to the API should use the mapping.

## Validation Rules

### Frontend (Display Layer)
- Spanish labels: `['Investigación', 'Extensión', 'Vinculación']`
- Can be none, one, or multiple selections
- Displayed in project table and project wizard

### Backend (API Layer)
- Enum values: `['INVESTIGACION', 'VINCULACION', 'EXTENSION']`
- Array of 0-3 values
- Stored as JSON array in database

### Response Handling
- ✅ Already working correctly with `mapSubTypeToString()` in `responseToProjectMapper.ts`
- Backend sends enum values → FE converts to Spanish for display

## Testing Checklist

After implementing the fix:

- [ ] Create a new project with one subtype → Check network tab shows correct enum value
- [ ] Create a new project with multiple subtypes → Check all are correct enum values
- [ ] Create a new project with no subtypes → Check empty array is sent
- [ ] Fetch an existing project → Check Spanish labels are displayed correctly
- [ ] No console errors related to subtype mapping
- [ ] ProjectTable displays subtypes correctly

## Files to Modify

1. **Create**: `src/mappers/subtypeMapper.ts` (or add to `src/components/projectWizard/types.ts`)
   - Add `mapSpanishSubtypeToEnum()` function
   - Add `mapSpanishSubtypesToEnum()` function

2. **Update**: `src/components/projectWizard/actions.ts`
   - Import mapping function
   - Convert `draft.subtypes` before sending to API

3. **Check**: `src/api/import.ts` (if exists)
   - If bulk import/update sends subtypes, apply mapping

4. **Check**: `src/api/publicApi.ts` (if exists)
   - If public API sends subtypes, apply mapping

## Important Notes

- DO NOT change `SUBTYPE_OPTIONS` - keep Spanish labels for user display
- DO NOT change the response mapper - keep converting enum values to Spanish for display
- ONLY change where subtypes are sent to the API
- The flow should be:
  ```
  Backend Enum (INVESTIGACION) 
  → Response Mapper to Spanish (Investigación) 
  → User sees Spanish in UI
  → User selects Spanish label
  → Action Mapper to Enum (INVESTIGACION)
  → Send to Backend
  ```

## Backend Status

✅ Backend is ready:
- Accepts: `INVESTIGACION`, `VINCULACION`, `EXTENSION`
- Rejects/ignores: `TYPE_1`, `TYPE_2`, Spanish labels
- Build: SUCCESSFUL
- No breaking changes

The fix is purely on the frontend to convert Spanish display values to backend enum values before sending.
