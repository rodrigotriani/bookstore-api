# Stage 07 — Final build and live API smoke tests

Before GitHub publication, validate the implemented application thoroughly.

## Build
1. Confirm Java runtime and compiler are Java 21, and Maven uses Java 21.
2. Execute `mvn clean verify` (tests required; do not use -DskipTests), fix failures and rerun until green or report blocker.
3. Inspect Surefire reports; capture exact passed/failed/error/skipped counts and Maven build result.
4. Verify no nested project and `pom.xml`, sources, README and .gitignore are in workspace root.

## Live HTTP smoke checks
5. Only if port 8080 is not occupied by an unrelated service, start **this** app (`mvn spring-boot:run`) under controlled process management. Do not kill unrelated processes. If 8080 is busy, use a safe temporary override port for checks and report the port used.
6. Verify actual HTTP statuses: GET `/api/health` (200, `{"status":"UP"}`), GET `/v3/api-docs` (200 JSON), GET `/swagger-ui/index.html` (200 or legitimate Swagger redirect leading to 200).
7. Run real Books → Customers → Orders API sequence with unique ISBN/email data; verify book stock reduced correctly and order total equals sum of item subtotals. Check returned statuses. This is demo test data in ephemeral H2.
8. Stop only the process started by these checks. Report HTTP endpoints not verified if execution was impossible.

## Readiness gate
- **Do not proceed to GitHub publication if `mvn clean verify` fails, tests are skipped, or a known severe business-rule defect remains.** Report blockers instead.
- If the build succeeds but live HTTP tests could not be run, report limitations explicitly and request a decision before publishing; do not misrepresent results.
- Save a factual list of all results for the final report and continue automatically to Stage 08 when ready.
