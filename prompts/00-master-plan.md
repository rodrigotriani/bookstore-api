# Bookstore API — IBM Bob Autonomous End-to-End Plan

## Mission
Build an entire eCommerce Bookstore REST API **from scratch**, from Maven/Spring Boot project initialization to verified GitHub publication. This workspace initially contains only `prompts/` (or other non-application setup artifacts). It is **not** a preexisting Spring Boot project. Use the current workspace root; do not create a nested `bookstore-api/` directory.

## Configuration
- Repository: `https://github.com/rodrigotriani/bookstore-api.git`
- Repository visibility: public; default branch: `main`; confirmed empty before preparation of this plan. Inspect remote state again before pushing.
- Project: `bookstore-api` (group `com.example`, artifact `bookstore-api`, base package `com.example.bookstore`, JAR).
- Java: 21; Spring Boot: **3.5.16**; Maven; Spring Web, Spring Data JPA, H2, Bean Validation, Spring Boot Starter Test, Springdoc OpenAPI UI compatible with Boot 3.5.x.
- HTTP port: 8080; H2 in-memory DB named `bookstoredb`.
- Modules: health, books, customers, orders; consistent error model; documentation and automated tests.

## Mandatory ordered stages
Read each file fully before executing it, and complete its tasks before continuing:
1. `prompts/01-architecture.md` — initialize project and define architecture.
2. `prompts/02-books.md` — Books CRUD.
3. `prompts/03-customers.md` — Customers CRUD.
4. `prompts/04-orders.md` — Orders, stock, transactions and concurrency.
5. `prompts/05-tests.md` — comprehensive regression/integration tests.
6. `prompts/06-documentation.md` — README, Swagger, .gitignore.
7. `prompts/07-final-validation.md` — compile, verify, smoke tests.
8. `prompts/08-github-publication.md` — inspect, commit and push to the user's public GitHub repository, verify publication.

## Global execution rules
1. Start from scratch: create `pom.xml`, Maven project tree, main class, configuration and health endpoint in Stage 01. Do not treat `prompts/` as an existing application. Do **not** assume any previous Java/Spring Boot files are correct or present.
2. First inventory the workspace without deleting anything. If an earlier Bookstore implementation, nontrivial Git history or existing remote files are detected, **stop rather than overwrite/reset**; explain the conflict. Do not use `rm -rf`, `git reset --hard`, force push or destructive cleanup.
3. Keep every source file in the current workspace root under `src/`. Use the exact package `com.example.bookstore`; follow Controller → Service → Repository layering, request/response DTOs, Bean Validation and standard JSON errors.
4. Use Java 21 for compilation and execution. If the shell defaults to another JDK, set `JAVA_HOME=$(/usr/libexec/java_home -v 21)` and prepend `$JAVA_HOME/bin` only for the active commands/session. If inherited `JAVA_TOOL_OPTIONS` points to a truststore from another project, unset it in the task process only. Never change global OS configuration.
5. Generate real working code, no stubs or TODO placeholders. Use BigDecimal for money and database uniqueness constraints where appropriate. Protect order inventory with a documented and tested transaction/concurrency strategy.
6. After each implementation stage run relevant Maven tests, fix errors and proceed in the stated order. Run `mvn clean verify` at the end. Never skip/falsify tests, logs or HTTP checks.
7. Work autonomously when tool permissions allow, without pausing between stages. Never bypass prompts for permissions or credentials. Stop for missing GitHub authentication, irreversible changes, existing remote history or other blockers; do not expose keys or request passwords in chat.
8. Public repository hygiene: never commit `.env`, secrets, machine-specific certificate stores, tokens, `target/`, `.idea/`, `.DS_Store` or local configuration paths. Do not copy anything from unrelated projects.
9. Include `prompts/` in the initial commit for **reproducibility and transparency**, unless the user's environment has explicit sharing restrictions. Files contain generic instructions only, not credentials. Do not modify prompt files while executing.
10. Do not push until full verification passes. Once verified, **push normally to `main`**, never force. If remote has commits, stop and report; do not merge/overwrite without approval. Only publish this designated repository.
11. Retain actual test counts, versions, HTTP results and Git evidence for the final report. Avoid verbose intermediate summaries; produce one consolidated report at the end.

## Completion criteria
- Bootstrapped Spring Boot Maven project, correct dependencies, health endpoint, H2 and Swagger UI.
- Books, Customers and Orders endpoints implemented with validations, CRUD/business behavior and order history protection.
- Transactional stock management with concurrency test and no negative stock.
- `mvn clean verify` succeeds; health, Swagger/OpenAPI and core flow smoke-tested whenever safe and feasible.
- Meaningful README, Git ignore hygiene, first commit pushed to `https://github.com/rodrigotriani/bookstore-api.git` on `main`, remote commit SHA verified.

If blocked: report exact step, command and evidence. Do not claim completion or publication until independently verified.
