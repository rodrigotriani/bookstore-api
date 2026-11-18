# Stage 06 — Documentation, Swagger and repository hygiene

## README.md (in English)
Document the **actual** implemented project:
1. Overview and technology stack with exact pinned Spring Boot and springdoc versions.
2. Java 21/Maven prerequisites, including optional macOS terminal-local `JAVA_HOME` setup.
3. Quick start: `mvn clean verify` and `mvn spring-boot:run`.
4. Feature-based package tree and Controller → Service → Repository responsibilities.
5. Endpoint table for Health, Books, Customers and Orders with expected statuses.
6. Swagger UI: `http://localhost:8080/swagger-ui/index.html`; OpenAPI: `/v3/api-docs`.
7. An end-to-end example: POST a book, POST a customer, POST an order (example JSON must match actual DTOs), GET book to check stock. Use valid request examples.
8. Explain H2 in-memory persistence resets on restart and is a demo database, not production persistence.
9. Explain order transactions, lock ordering, stock validation, price snapshots, error conventions and any limitations.
10. How to run tests and what they cover; do not invent metrics.
11. Repository link: `https://github.com/rodrigotriani/bookstore-api`.

## OpenAPI
- Ensure controllers/DTOs have clear descriptions, response codes, examples and grouped operation tags as appropriate.
- No security-sensitive endpoints or unnecessary H2 console exposure on public deployments.

## Git hygiene
- `.gitignore` must exclude `target/`, IDE settings, OS artifacts, environment/secrets, `*.jks`, `*.p12`, credential files and log files; avoid ignoring source files.
- `prompts/` is intentionally included for reproducibility; inspect for secrets before commit. Do not put personal tokens or private paths in documentation.
- Do not run git push in this stage.

Continue automatically to Stage 07.
