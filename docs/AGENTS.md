# HackHub Agent Instructions

These instructions apply to the repository root and to the Spring Boot project in
`HackHub/`.

## Project Context

HackHub is a university software engineering project for managing hackathons.
The implementation is a Java Spring Boot backend and must remain coherent with
the Visual Paradigm artifacts developed for the iterative process: use case
diagrams, analysis class diagrams, design class diagrams, and related
documentation.

The platform manages hackathons, teams, staff assignments, submissions,
mentoring requests, judging, winner proclamation, and external prize payment.
Every hackathon follows this lifecycle:

- `REGISTRATION`: teams can register before the registration deadline.
- `RUNNING`: admitted teams work and can request mentoring support.
- `EVALUATION`: judges evaluate submitted work.
- `CONCLUDED`: the organizer proclaims exactly one winning team.

The main actors from the trace and first-iteration use case diagram are:

- `Visitor`: unauthenticated user who can browse public hackathon information
  and can register or log in.
- `User`: authenticated account that can create a team, invite users, accept
  invitations, and browse hackathons.
- `TeamMember`: user acting through their team, with team registration and
  submission permissions.
- `StaffMember`: base staff role assigned to specific hackathons.
- `Organizer`: staff member who creates hackathons, manages staff, manages
  hackathon state, and proclaims the winner.
- `Mentor`: staff member who handles mentoring requests, proposes calls through
  an external calendar, and reports rule violations.
- `Judge`: staff member who reviews submissions and assigns written feedback
  plus a numeric score.
- `CalendarSystem`: external system used only to schedule mentor/team calls.
- `PaymentSystem`: external system used only to pay the prize to the winning
  team.

## Domain Language

Use English names in Java code and APIs. Keep the meaning aligned with the
Italian trace and Visual Paradigm diagrams.

Preferred domain names:

- `Hackathon`, not `Hackaton`, even when the diagram text contains the typo.
- `Team`, `TeamMember`, `Submission`, `Evaluation`, `Invitation`.
- `StaffMember`, `Organizer`, `Mentor`, `Judge`.
- `SupportRequest`, `MentoringCallProposal`, `ViolationReport`.
- `CalendarGateway` or `CalendarAdapter` for the external calendar.
- `PaymentGateway` or `PaymentAdapter` for the external payment system.

Documentation may mention the Italian actor and use case names when it improves
traceability with Visual Paradigm, but code should remain consistently English.

## Current Technical Baseline

- Main project directory: `HackHub/`.
- Base Java package: `it.unicam.cs.ids.hackhub`.
- Runtime stack: Java 21, Spring Boot 4.0.6, Maven Wrapper.
- Standard command: run Maven from `HackHub/` with `./mvnw test`.
- Persistence: Spring Data JPA with H2 during local development.
- API documentation: Springdoc OpenAPI is configured for the base package.
- Mapping: MapStruct is available and should be used for DTO/entity mapping.
- Security: Spring Security and JJWT are available, but JWT is not fully
  integrated yet.

Do not downgrade Spring Boot, Java, or Maven Wrapper versions unless there is a
verified incompatibility and the change is explicitly justified.

## Architecture Rules

Implement HackHub as a Spring Boot backend with layered MVC architecture:

- `controller`: REST controllers, HTTP endpoints, input validation, and
  `ResponseEntity` creation.
- `dto`: request and response DTOs; never expose JPA entities directly through
  the API.
- `model`: JPA entities, enums, value objects, and domain classes.
- `model/repository`: Spring Data JPA repositories only.
- `service/interfaces`: service contracts used by controllers and other
  services.
- `service/impl`: service implementations and business logic.
- `service/mapper`: MapStruct mappers between entities and DTOs.
- `security`: Spring Security configuration, JWT components, filters, and
  authorization rules.
- `exception`: custom exceptions and the global API exception handler.
- `config`: application configuration that is not security-specific.
- `validation`: custom validators and validation annotations.

Controllers must stay thin. They should validate input, delegate to services,
and translate service results to HTTP responses. Business rules belong in
services, persistence queries belong in repositories, and mapping belongs in
MapStruct mappers.

## Coding Rules

- Follow SOLID principles and keep classes focused on one responsibility.
- Prefer constructor injection for dependencies.
- Keep public service APIs explicit through interfaces in `service/interfaces`.
- Use DTOs for every request and response boundary.
- Use Bean Validation annotations on request DTOs; add custom validators only
  for rules that cannot be expressed clearly with standard annotations.
- Use `GlobalExceptionHandler` with `@RestControllerAdvice` for consistent
  error responses.
- Avoid leaking persistence details into controllers or DTOs.
- Avoid hard-coded external-system behavior inside business services.
- Keep naming coherent with Visual Paradigm diagrams when implementing use
  cases from the iterative process.
- Add succinct comments only when they clarify non-obvious domain decisions.

## Domain Constraints

Preserve these core rules in services and tests:

- A registered user can belong to at most one team at any time.
- Each team has exactly one leader, modeled as a `TeamMember` with role
  `TeamRole.LEADER`. The leader is also a regular member of the team.
- Only the leader can delete the team, send invitations, or remove members.
- Leadership is transferred only via the `leaveTeam` use case: the current
  leader designates a successor among existing team members, and the same
  transaction promotes the successor and removes the outgoing leader.
- A leader who is the only team member cannot leave; they must use `deleteTeam`
  to dissolve the team.
- A team can be deleted only if all its hackathon registrations are in
  `REGISTRATION` state (i.e., the hackathon has not yet started).
- A team can register for a hackathon only while the hackathon is in the
  registration phase and before the registration deadline.
- A submission can be uploaded and updated only before the submission deadline
  defined for the hackathon.
- Staff members can view submissions only for hackathons to which they are
  assigned.
- A judge can evaluate only submissions for hackathons assigned to that judge.
- An evaluation contains a short written judgment and a numeric score from 0 to
  10 inclusive.
- An organizer can proclaim the winner only after all submissions for that
  hackathon have been judged.
- Exactly one team can be proclaimed winner for a concluded hackathon.
- Mentors can report suspected rule violations to the organizer.
- Mentor/team call booking is delegated to the external calendar system.
- Prize payment is delegated to the external payment system after winner
  proclamation.

## Security Rules

Spring Security is part of the project architecture. JWT-based authentication
and role/assignment-based authorization are expected in a later implementation.

The current permissive `SecurityConfig` is only a temporary development bypass
while JWT is not integrated. Do not treat `anyRequest().permitAll()` as the
final security design.

When security is implemented:

- Keep authentication logic in `security`.
- Protect team actions so only authorized team members can act for their team.
- Protect staff actions by both role and hackathon assignment.
- Keep public hackathon consultation available to visitors.
- Keep H2 console access development-only.

## External Systems

Calendar and payment are external systems. Model them behind interfaces so the
core domain can be tested without real third-party integrations.

Recommended approach:

- Define gateway interfaces for required operations.
- Implement adapters in infrastructure/configuration-oriented packages.
- Inject gateways into services that need scheduling or payment behavior.
- Use test doubles for service tests.

Do not place HTTP client details, external URLs, or provider-specific payloads
inside core domain services.

## Design Patterns

The project must use at least two design patterns different from Singleton.
Prefer patterns that naturally support the trace:

- `Strategy`: lifecycle transition rules, submission scoring policies, or
  authorization checks that vary by role/state.
- `Adapter`: integration with the external calendar and payment systems.
- `Factory Method`: creation of role-specific staff behavior or domain command
  objects when it removes duplication.
- `State`: hackathon lifecycle behavior if state-specific rules become too
  complex for a simple enum plus service validation.

Document pattern usage in code structure and project documentation so it remains
visible for the university deliverables.

## Testing and Verification

For code changes, run verification from `HackHub/`:

```bash
./mvnw test
```

Add focused tests when implementing a use case:

- Service tests for business rules and domain constraints.
- Controller tests for HTTP status codes, validation, and DTO boundaries.
- Repository tests only for custom queries.
- Security tests when authorization rules are introduced.
- Adapter tests with mocked external systems for calendar and payment.

For documentation-only changes, Maven tests are not required unless code or
configuration changed. Still verify Git visibility and ignore rules when adding
files under `docs/`.

## Git and Repository Hygiene

- Keep generated build output such as `HackHub/target/` ignored.
- Keep local IDE state ignored unless the file is intentionally part of project
  setup.
- `docs/AGENTS.md` must be tracked because it defines collaboration rules for
  future work.
- Do not commit local H2 database files unless the user explicitly asks for
  sample data.

Before finishing a code task, check:

```bash
git status --short --ignored
```

Confirm that only intentional source, config, or documentation files are ready
to be tracked.
