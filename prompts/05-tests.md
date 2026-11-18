# Stage 05 — Regression, integration and concurrency tests

Review test coverage from all prior stages. Keep tests independent and meaningful; use JUnit 5, Mockito, MockMvc and Spring Boot Test / H2 integration tests as appropriate.

## Required test coverage
- Health `GET /api/health` → HTTP 200, `{"status":"UP"}`.
- Books CRUD and HTTP status codes, invalid data, duplicate ISBN, unknown id.
- Customers CRUD, invalid/duplicate/normalized email, unknown id.
- Orders creation with multiple books, correct arithmetic and price snapshot behavior.
- Inventory decrement, 409 when insufficient, rollback when any line item fails, no orphan items.
- 400 for duplicate book IDs within one order; 400 for invalid request payloads.
- Deletion protection for referenced books/customers.
- Concurrent orders contending for one remaining book; ensure **actual overlapping transactions** and never both succeed; verify stock >= 0. Avoid flaky tests relying only on arbitrary sleeps.

## Quality and execution
- Run `mvn clean test`; repair real defects. Do not disable/drop a failing test to force success.
- Handle Mockito/Byte Buddy warnings if practical and harmless; do not compromise runtime or suppress failures.
- Count actual executed, passed, failed and skipped tests from output/surefire reports and retain for final summary.
- Then continue automatically to Stage 06.
