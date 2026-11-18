# Stage 03 — Customers CRUD

Implement Customers within the same Java package layout and API error style.

## Entity
- `Customer`: `id` (Long generated), `name` (required nonblank), `email` (required valid format, unique), `phone` (optional).
- Normalize email consistently (e.g., trim and lowercase) before persistence and lookup; document this policy. Use appropriate string constraints and a DB unique constraint.

## Endpoints
- `POST /api/customers` → 201 Created.
- `GET /api/customers` → 200 list.
- `GET /api/customers/{id}` → 200 or 404.
- `PUT /api/customers/{id}` → 200, 400, 404 or 409.
- `DELETE /api/customers/{id}` → 204 or 404. If a customer has orders, preserve order history by refusing deletion with 409.

## Deliverables
- Feature package with controller, service, repository, request/response DTOs, validation and OpenAPI documentation.
- Tests for CRUD, invalid email, duplicate email (including normalization), missing customer, and deletion constraints after Orders exists.
- Ensure Books and Health tests continue passing.

Run `mvn test`, correct errors and continue to Stage 04.
