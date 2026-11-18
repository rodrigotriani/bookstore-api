# Bookstore API

A REST API for a small online bookstore: manage **books**, **customers** and **orders**, with transactional stock management, input validation and centralized error handling.

Repository: <https://github.com/rodrigotriani/bookstore-api>

## Technology stack

| Component | Version |
|-----------|---------|
| Java | 21 |
| Spring Boot | 3.5.16 (Web, Data JPA, Validation) |
| springdoc-openapi-starter-webmvc-ui | 2.8.14 |
| Database | H2 (in-memory) |
| Build | Maven |
| Tests | JUnit 5, Mockito, AssertJ, Spring Boot Test / MockMvc |

## Prerequisites

- JDK 21
- Maven 3.9+

On macOS, if your shell defaults to another JDK, set Java 21 for the current terminal session only:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
```

## Quick start

```bash
mvn clean verify        # compile and run all tests
mvn spring-boot:run     # start the API on http://localhost:8080
```

## Project structure

```
src/main/java/com/example/bookstore/
  BookstoreApiApplication.java
  health/     HealthController
  book/       Book, BookRepository, BookService, BookController, BookRequest, BookResponse
  customer/   Customer, CustomerRepository, CustomerService, CustomerController, CustomerRequest, CustomerResponse
  order/      BookOrder, OrderItem, OrderRepository, OrderItemRepository, OrderService, OrderController, DTOs
  common/error/  ApiError, GlobalExceptionHandler, ResourceNotFoundException, ConflictException, BadRequestException
src/main/resources/application.properties
src/test/java/com/example/bookstore/...
```

Layering:

- **Controller** – thin HTTP layer: binds and validates DTOs (`@Valid`), delegates to the service, sets status codes and OpenAPI docs.
- **Service** – business rules and transaction boundaries (uniqueness, stock, deletion protection, price snapshots).
- **Repository** – Spring Data JPA persistence. JPA entities are never exposed over REST; only request/response DTOs are.

## Endpoints

| Method | Path | Success | Errors |
|--------|------|---------|--------|
| GET | `/api/health` | 200 `{"status":"UP"}` | |
| POST | `/api/books` | 201 (+ `Location`) | 400, 409 duplicate ISBN |
| GET | `/api/books` | 200 | |
| GET | `/api/books/{id}` | 200 | 404 |
| PUT | `/api/books/{id}` | 200 | 400, 404, 409 duplicate ISBN |
| DELETE | `/api/books/{id}` | 204 | 404, 409 book is in an order |
| POST | `/api/customers` | 201 (+ `Location`) | 400, 409 duplicate email |
| GET | `/api/customers` | 200 | |
| GET | `/api/customers/{id}` | 200 | 404 |
| PUT | `/api/customers/{id}` | 200 | 400, 404, 409 duplicate email |
| DELETE | `/api/customers/{id}` | 204 | 404, 409 customer has orders |
| POST | `/api/orders` | 201 (+ `Location`) | 400 invalid / repeated book ids, 404 unknown customer or book, 409 insufficient stock |
| GET | `/api/orders` | 200 | |
| GET | `/api/orders/{id}` | 200 | 404 |

## Swagger / OpenAPI

- Swagger UI: <http://localhost:8080/swagger-ui/index.html> (`/swagger-ui.html` redirects there)
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>

Operations are grouped by tag (Health, Books, Customers, Orders) with response codes and field examples. The H2 console is disabled. Swagger UI and `/v3/api-docs` are public and unauthenticated; disable them (`springdoc.api-docs.enabled=false`, `springdoc.swagger-ui.enabled=false`) for any public deployment.

## End-to-end example

```bash
# 1. Create a book
curl -s -X POST localhost:8080/api/books -H 'Content-Type: application/json' -d '{
  "title": "Clean Code", "author": "Robert C. Martin",
  "isbn": "9780132350884", "price": 39.90, "stockQuantity": 10 }'
# -> {"id":1,"title":"Clean Code",...,"price":39.90,"stockQuantity":10}

# 2. Create a customer
curl -s -X POST localhost:8080/api/customers -H 'Content-Type: application/json' -d '{
  "name": "Ada Lovelace", "email": "ada@example.com", "phone": "+44 20 7946 0000" }'
# -> {"id":1,"name":"Ada Lovelace","email":"ada@example.com",...}

# 3. Place an order for 2 copies
curl -s -X POST localhost:8080/api/orders -H 'Content-Type: application/json' -d '{
  "customerId": 1, "items": [ { "bookId": 1, "quantity": 2 } ] }'
# -> 201 {"id":1,"customer":{...},"createdAt":"...","items":[{"bookId":1,"quantity":2,"unitPrice":39.90,"subtotal":79.80,...}],"total":79.80}

# 4. Stock is now 8
curl -s localhost:8080/api/books/1
```

## Data and persistence

H2 runs **in-memory** (`jdbc:h2:mem:bookstoredb`) with user `sa` and a blank password. It is a local demo database: **all data is lost when the application stops**, and it is not suitable for production. Hibernate `ddl-auto=update` is also demo-only; use a real database and schema migrations (Flyway/Liquibase) for production.

## Business rules and design notes

- **Order transaction** – `OrderService.create` is a single Spring `@Transactional` method that validates input, checks stock, decrements stock and persists the order and its items. Any exception rolls everything back (tested by forcing a failure while persisting).
- **Concurrency / lock ordering** – requested books are loaded with `SELECT ... FOR UPDATE` (JPA `PESSIMISTIC_WRITE`) in ascending id order, inside the transaction. Stock is checked and decremented while the locks are held, so concurrent orders cannot oversell and stock never goes negative. The consistent ordering prevents deadlocks between orders listing books in different orders. The H2 URL sets `LOCK_TIMEOUT=10000`; a lock timeout is reported as 409 "Resource is busy, please retry".
- **Repeated book ids** in one order are rejected with **400** (send one line per book with the total quantity).
- **Price snapshots** – each order item stores `unitPrice` and `subtotal` at purchase time; later book price changes never alter historical orders. Money uses `BigDecimal` with scale 2.
- **Order history protection** – books and customers referenced by orders cannot be deleted (409). Cascades on orders only persist items; nothing cascades deletes to books or customers.
- **Uniqueness** – ISBN and email are unique both in the service (409) and via database unique constraints (race conditions are mapped to 409).
- **Email normalization** – emails are trimmed and lowercased before validation, lookup and storage, so `Ada@Example.com ` and `ada@example.com` are the same customer.
- **Error model** – every error returns JSON: `timestamp`, `status`, `error`, `message`, `path`, `details` (field errors for validation failures). Stack traces and internals are never exposed; unexpected errors return a generic 500 and are logged server-side.

### Limitations

- No authentication or authorization, payments or pagination (out of scope for this demo).
- Pessimistic locking is guaranteed within one database; H2 in-memory is single-instance.

## Tests

```bash
mvn clean test     # or: mvn clean verify
```

Coverage:

- Health endpoint and OpenAPI document availability.
- Books and Customers: CRUD, validation errors, duplicate ISBN / email (including normalization), unknown ids, deletion protection.
- Orders: multi-book orders, arithmetic, price snapshots, stock decrement, 409 on insufficient stock with full rollback and no orphan rows, 400 on repeated book ids and invalid payloads, 404 on unknown customer/book.
- Service-level unit tests with Mockito for order and customer rules.
- Concurrency: a deterministic test where a second order blocks while another transaction holds the book lock, a test with 12 simultaneous buyers competing for 3 units (exactly 3 succeed), and a lock-ordering deadlock test.
