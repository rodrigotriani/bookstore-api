# Stage 02 — Books CRUD

Build a functional Books REST module using the project initialized in Stage 01.

## Entity and persistence
- `Book`: `id` (Long, generated), `title` (required nonblank), `author` (required nonblank), `isbn` (required nonblank and unique), `price` (BigDecimal > 0; database decimal precision/scale), `stockQuantity` (int >= 0).
- Add `@Version` only if useful to the chosen concurrency strategy; preserve consistency with Stage 04. ISBN unique at both service/API and database levels; validate request strings and field lengths.
- Use request/response DTOs; never expose entities directly. Use stable JSON field names above.

## Endpoints
- `POST /api/books` → 201 Created with book response and Location header if practical.
- `GET /api/books` → 200 list.
- `GET /api/books/{id}` → 200 detail; 404 unknown id.
- `PUT /api/books/{id}` → 200 updated; 400 invalid; 404 unknown; 409 duplicate ISBN.
- `DELETE /api/books/{id}` → 204; 404 unknown; and 409 when referenced by order history (once Orders exists).

## Implementation
- Controller → Service → Repository, DTO validation (`@Valid`), centralized JSON error handler shared with other modules.
- Return correct 400, 404 and 409 HTTP statuses. Handle uniqueness races (database exception mapping) without exposing raw stack traces.
- Annotate operations/responses with OpenAPI details.
- Add tests for CRUD, validation, duplicate ISBN and missing book (service + HTTP where useful).
- Preserve the health endpoint.

Run `mvn test` and fix failures before Stage 03. Record real results for the final report.
