# ADR 001: Architectural Foundation of the Tenant Service (Hexagonal, Reactive, and Strict Domain Patterns)

## Status
Accepted

## Context
The Tenant bounded context is the foundational microservice of the `com.thinklab` platform. It requires a robust, testable, and scalable architecture capable of handling high-throughput asynchronous operations while maintaining absolute data integrity and forensic auditability.

To achieve "NASA-level" reliability, we must avoid common pitfalls associated with monolithic designs, weak domain typing, proxy-based dependency injection conflicts, and database upsert collisions. We need a clear separation between business logic and infrastructure concerns.

## Decision
We have decided to implement a **Hexagonal Architecture (Ports and Adapters)** combined with a **Reactive Stack** (Micronaut, Project Reactor, and MongoDB Reactive Streams), governed by strict typing and mutational rules.

### Key Components:
1. **Hexagonal Layers:**
    - **Domain:** Pure Java 21 Records and Objects representing business rules (e.g., `Tenant`, `TenantProfile`), isolated from any framework dependency.
    - **Application:** Use Cases (Interactors) acting as Input Ports to orchestrate business flows.
    - **Infrastructure:** Adapters (Repositories, Controllers) implementing Output Ports to communicate with external systems.
2. **Native Strong Typing:** `java.util.UUID` is adopted natively across all layers (Domain Aggregates, Infrastructure Entities, and Application Commands). String representations are restricted exclusively to JSON serialization boundaries (REST Controllers).
3. **Explicit Dependency Injection:** The use of Lombok's `@RequiredArgsConstructor` is strictly banned on all Micronaut managed beans (e.g., `@Singleton`, `@Repository`). All dependencies must be injected via explicit constructors annotated with `jakarta.inject.Inject` to ensure Ahead-of-Time (AoT) compilation compatibility and prevent `BeanInstantiationException`.
4. **Strict Mutational Contracts:** Output Ports (e.g., `TenantRepositoryPort`) must clearly segregate lifecycle operations:
    - `save()`: Restricted strictly to the initial provisioning (creation) of an Aggregate.
    - `update()`: Mandatory for all subsequent state transitions (e.g., Suspend, Reactivate, Revoke, Profile Updates) to enforce MongoDB `replaceOne` behavior and prevent `E11000 duplicate key` collisions.

## Consequences

### Positive:
- **Testability:** Business logic is 100% testable using JUnit 5 and Mockito without needing a running database or HTTP container.
- **Resilience & Stability:** Complete elimination of DI-related startup crashes and database duplicate key errors during state mutations. Non-blocking I/O optimizes resource usage under high load.
- **Domain Integrity:** The Domain layer is protected against malformed strings masquerading as identifiers.
- **Traceability:** Explicit constructors make dependency graphs visibly clear. Every state mutation is immutably linked to a forensic audit trail (`TenantAudit`).

### Negative:
- **Complexity & Boilerplate:** Increased initial boilerplate due to manual constructor writing, interfaces, DTOs, and mapping layers between Domain and Infrastructure.
- **Cognitive Load:** Developers must consciously decide between `save()` and `update()` when handling reactive streams, and maintain strict boundary segregation.

## Implementation Details
- **Idempotency:** `CreateTenantInteractor` utilizes deterministic UUID generation (`UUID.nameUUIDFromBytes` based on TaxID + Country) prior to persistence.
- **Boundary Defense:** All incoming requests must pass through immutable Java `records` (Commands) validated by Jakarta Bean Validation (`@Valid`, `@NotBlank`, `@NotNull`).
- **Error Handling:** Granular hierarchy separating Technical Exceptions (infrastructure) from Business Exceptions (logic).

## Compliance
- All new code must adhere to the `com.thinklab` package structure defined in the architecture audit.
- CI/CD pipelines and Code Reviews must flag and reject `@RequiredArgsConstructor` on Micronaut components.
- Any modification to an existing Aggregate Root must invoke the `.update()` port method.