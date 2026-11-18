# Stage 01 — Initialize project, architecture and baseline

Act as a Senior Java/Spring Boot architect and implement the project **from scratch** in the current workspace root.

## Preconditions
1. List current workspace files and check `git status` if Git is initialized. The workspace is intended to contain `prompts/` but no application implementation. If `pom.xml`, `src/` or unrelated preexisting project code/history exists, pause instead of overwriting it. Never delete existing files to fabricate a clean slate.
2. Confirm `java -version`, `javac -version` and `mvn -version` use Java 21 for this session. On macOS set JAVA_HOME to `/usr/libexec/java_home -v 21` and update PATH if needed. Unset `JAVA_TOOL_OPTIONS` only in the task session when inherited from another project and potentially interfering with Maven. Do not modify `.zshrc` or system-wide settings.

## Maven / application initialization
- Create `pom.xml` in this workspace root (NOT in a nested directory).
- Maven coordinates: `com.example:bookstore-api:0.0.1-SNAPSHOT`, packaging `jar`.
- Use `spring-boot-starter-parent` **3.5.16**, `<java.version>21</java.version>`.
- Add `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, runtime H2, and `spring-boot-starter-test` (test scope).
- Add a specific **compatible springdoc-openapi-starter-webmvc-ui 2.8.x** version; pin the exact chosen version in `pom.xml` instead of using a version range or `LATEST`. Confirm compatibility using Maven resolution.
- Configure `spring-boot-maven-plugin`. Add Maven Wrapper files only if they can be generated reliably and securely; otherwise standard Maven commands are sufficient.
- Main class: `src/main/java/com/example/bookstore/BookstoreApiApplication.java` with `@SpringBootApplication`.
- `application.properties`: `spring.application.name=bookstore-api`; port `8080`; `jdbc:h2:mem:bookstoredb;DB_CLOSE_DELAY=-1`; username `sa`; blank H2 password for **local in-memory demo only**; Hibernate `ddl-auto=update` (document demo-only setting); enable `/h2-console` only if useful for local demo, not required. H2 is ephemeral.
- Create `GET /api/health` returning `200` JSON `{"status":"UP"}` via controller and response DTO/map.
- Add meaningful tests, including MockMvc health endpoint `200` response.
- Add `.gitignore` for Java/Maven/IDE/local files at this stage, refine in Stage 06.

## Architecture / file layout
Use feature-focused packages and shared infrastructure:
```
src/
  main/
    java/com/example/bookstore/
      BookstoreApiApplication.java
      health/HealthController.java
      book/{Book.java,BookRepository.java,BookService.java,BookController.java,...}
      customer/{Customer.java,CustomerRepository.java,CustomerService.java,CustomerController.java,...}
      order/{BookOrder.java,OrderItem.java,OrderRepository.java,OrderService.java,OrderController.java,...}
      common/error/{ApiError.java,GlobalExceptionHandler.java,...}
    resources/application.properties
  test/java/com/example/bookstore/{...}
```
You may place DTOs in feature subpackages. No JPA entity exposure over REST. Controllers are thin; services enforce business rules; repositories handle persistence. Responses/errors should be consistent across modules. Avoid reserved SQL keywords (`order`, `user`) as table names.

## Verify and proceed
- Generate the baseline project and run `mvn clean test`, fix issues, record actual results.
- Do not implement Books, Customers or Orders here. Continue automatically to Stage 02.
