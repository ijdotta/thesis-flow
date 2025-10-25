# Dataset Import API

Use this endpoint to upload a legacy CSV file (`dataset.csv` format) and create projects, people, tags, and domains in bulk. Each row is processed independently so the frontend can review successes and failures in a single response.

## Endpoint

```
POST /bulk/dataset/projects
Content-Type: multipart/form-data
Authorization: Bearer <admin-token>
```

The request must include a single multipart part named `file` containing the CSV payload. The backend currently only allows administrators (`ROLE_ADMIN`) to call this endpoint.

## CSV Requirements

The service expects the same header layout as `src/main/resources/data/dataset.csv`:

```
Tipo de proyecto,Fecha Consejo,Fecha Finalizacion,Completado,Titulo,Director,Co-Director,Colaborador,Alumno 1,Alumno 2,Alumno 3,Tema(s),Area o Dominio de Aplicacion
```

Key mapping rules:

- `Tipo de proyecto`: `PF` → `FINAL_PROJECT`, `TL` → `THESIS`. Any other value is treated as an error.
- `Fecha Consejo`: project initial submission (required).
- `Fecha Finalizacion`: project completion (optional).
- `Director`, `Co-Director`, `Colaborador`: mapped to participants with roles `DIRECTOR`, `CO_DIRECTOR`, `COLLABORATOR`.
- `Alumno 1/2/3`: mapped to `STUDENT` participants. Any combination of one to three students per row is supported.
- `Tema(s)`: converted into tags (duplicates are merged, names are normalized for casing and whitespace).
- `Area o Dominio de Aplicacion`: mapped to `ApplicationDomain`s (created on demand).

Missing emails and student IDs are filled with deterministic placeholder values so they are easy to spot (`professor+<slug>@cs.uns.edu.ar`, `student+<slug>@legacy.thesisflow`, `legacy-<slug>` IDs).

All imported projects are associated to a synthetic `Career` named `Legacy Dataset`. The service reuses any existing people, professors, students, tags, and domains before creating new entries.

## Response Shape

```json
{
  "summary": {
    "total": 3,
    "success": 2,
    "skipped": 1,
    "failed": 0
  },
  "results": [
    {
      "lineNumber": 2,
      "title": "Localizador de Dispositivos Móviles",
      "status": "SUCCESS",
      "project": {
        "publicId": "9d1d...",
        "title": "Localizador de Dispositivos Móviles",
        "type": "FINAL_PROJECT",
        "subtype": [],
        "initialSubmission": "2011-06-14",
        "completion": "2011-09-14",
        "career": {
          "publicId": "...",
          "name": "Legacy Dataset"
        },
        "applicationDomainDTO": {
          "publicId": "...",
          "name": "Desarrollo Mobile"
        },
        "tags": [ { "publicId": "...", "name": "Desarrollo Mobile" } ],
        "participants": [
          { "role": "DIRECTOR", "personDTO": { "publicId": "...", "name": "Alimenti", "lastname": "Cayssias - Castro" } },
          { "role": "STUDENT", "personDTO": { "publicId": "...", "name": "Pablo", "lastname": "Fullana" } }
        ]
      },
      "message": "Imported successfully"
    },
    {
      "lineNumber": 3,
      "title": "Localizador de Dispositivos Móviles",
      "status": "SKIPPED",
      "project": { "publicId": "9d1d...", "title": "Localizador de Dispositivos Móviles", ... },
      "message": "Project already exists for 2011-06-14"
    },
    {
      "lineNumber": 4,
      "title": "Sistema desconocido",
      "status": "FAILED",
      "project": null,
      "message": "Unknown project type 'XX'"
    }
  ]
}
```

- `lineNumber` comes from the CSV parser (header row = 1). This lets the UI map results back to the original sheet.
- `status` is one of:
  - `SUCCESS`: row persisted successfully.
  - `SKIPPED`: duplicate detected (title + initial submission already imported). The existing project is returned so the UI can display current data.
  - `FAILED`: row could not be processed. `message` explains why.
- `project` is the enriched `ProjectDTO`, including public IDs, tags, domain, and participant information when available.
- `message` provides additional context (success note, skip reason, or error description).

## Usage Example

```bash
curl -X POST http://localhost:8080/bulk/dataset/projects \
  -H "Authorization: Bearer <ADMIN_JWT>" \
  -F "file=@/path/to/dataset.csv"
```

## Error Handling

- `400 Bad Request` – the `file` part is missing or empty.
- `401/403` – authentication or authorization failure (admin role required).
- `500 Internal Server Error` – unexpected server error (the response body will include the standard error payload).

## Additional Notes

- The importer is tolerant: one bad row does not roll back previous successes.
- The service performs light normalization (whitespace trimming, casing) but does not attempt fuzzy matching beyond exact name comparisons.
- When duplicate rows are sent again, they are reported as `SKIPPED`; use the returned `project.publicId` to navigate or update the existing record.
- You can still run the legacy bootstrap import at startup by setting `app.dataset.import.enabled=true` in the configuration.
