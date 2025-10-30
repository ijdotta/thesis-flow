# Backup & Restore Feature

This feature allows ADMIN users to backup and restore the entire database state.

## Endpoints

### Create Backup
```
GET /api/backup/create
```

**Response**: JSON file with all database tables and their data.

**Format**:
```json
{
  "table_name1": [
    { "id": 1, "publicId": "uuid-1", ... },
    { "id": 2, "publicId": "uuid-2", ... }
  ],
  "table_name2": [...]
}
```

**Security**: Requires ADMIN role
**Usage**: The response includes a `Content-Disposition` header for direct download as `thesis-flow-backup.json`

### Restore Backup
```
POST /api/backup/restore
Content-Type: application/json

{backup JSON content}
```

**Security**: Requires ADMIN role
**Behavior**: 
1. Clears all existing data (in reverse dependency order)
2. Imports all tables from the backup JSON
3. Preserves entity IDs to maintain relationships

**Response**:
```json
{
  "message": "Backup restored successfully"
}
```

## Backup Format

The backup includes all tables:
- career
- person
- professor
- student
- student_career
- auth_user
- professor_login_token
- application_domain
- tag
- project
- project_participant

## Important Notes

- **Entity IDs**: Entity IDs are preserved during backup/restore to maintain relationships
- **Public IDs**: All UUIDs are preserved for referential integrity
- **Order of Operations**: Tables are restored in dependency order to avoid FK constraint violations
- **Data Clearing**: When restoring, all existing data is deleted first
- **Admin Only**: Both operations are restricted to ADMIN role

## Example Usage

```bash
# Create backup
curl -H "Authorization: Bearer YOUR_TOKEN" \
  https://thesis-flow.onrender.com/api/backup/create \
  -o backup.json

# Restore from backup
curl -X POST \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d @backup.json \
  https://thesis-flow.onrender.com/api/backup/restore
```
