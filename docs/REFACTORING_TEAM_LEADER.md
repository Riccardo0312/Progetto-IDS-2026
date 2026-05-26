# Refactoring: Team Leader come ruolo (branch `angelo-4`)

## Problema

Il modello precedente non aveva un concetto esplicito di leader del team.
La leadership era simulata tramite il campo `Team.creator: User`, usato come
proxy in `InvitationService` per decidere chi poteva invitare nuovi membri.
Questo approccio era sbagliato perché:

- `creator` e "leader corrente" sono semantiche diverse (il fondatore non cambia,
  il leader può cambiare).
- Nessuna regola di dominio era applicabile in modo uniforme (chi può eliminare
  il team? chi può rimuovere membri?).
- Non c'era modo di trasferire la leadership senza introdurre logica ad hoc.

## Soluzione

Il leader è modellato come **ruolo** all'interno di `TeamMember`, tramite un enum:

```java
public enum TeamRole { LEADER, MEMBER }
```

Ogni `TeamMember` ha un campo `role` persistito come stringa (`@Enumerated(EnumType.STRING)`).
Il creatore del team diventa automaticamente il primo `LEADER`. Non esiste una classe
`TeamLeader` separata né un flag booleano `isLeader`.

---

## Modifiche per commit

### Commit 1 — `feat: add TeamRole enum and role field to TeamMember`

**File modificati:** `TeamRole.java` (nuovo), `TeamMember.java`, `TeamMemberRepository.java`

- Aggiunto enum `TeamRole { LEADER, MEMBER }`.
- `TeamMember` ora ha campo `role` (`NOT NULL`, default `MEMBER`), costruttore
  di dominio `TeamMember(user, team, role)`, metodi `isLeader()` e `isMember()`.
- `TeamMemberRepository` esteso con:
  - `existsByTeamIdAndUserIdAndRole`
  - `findByTeamIdAndRole`
  - `countByTeamIdAndRole`
  - `findByTeamIdAndUserId`

---

### Commit 2 — `refactor(team): replace Team.creator with leader-via-role helpers`

**File modificati:** `Team.java`, `TeamService.java`

- Rimosso campo `creator: User` da `Team`.
- Aggiunti helper di dominio su `Team`:
  - `findLeader()` → `Optional<TeamMember>`
  - `getLeader()` → `TeamMember` (lancia `IllegalStateException` se assente)
  - `hasLeader()` → `boolean`
  - `isLedBy(userId)` → `boolean`
  - `promoteToLeader(target)` → swap atomico della leadership
- `TeamService.createTeam`: il primo `TeamMember` creato ha `role = LEADER`.

---

### Commit 3 — `refactor(invitation): leader-only check via TeamRole.LEADER`

**File modificati:** `InvitationService.java`

- Sostituito il check `team.getCreator()` con `existsByTeamIdAndUserIdAndRole(..., LEADER)`.
- Chi non è leader riceve `ForbiddenOperationException` (HTTP 403) invece di
  `IllegalArgumentException` (HTTP 400).
- `acceptInvitation`: il nuovo membro viene creato con `role = MEMBER` (mai LEADER).

---

### Commit 4 — `feat(team): atomic leaveTeam with successor for leader`

**File modificati:** `ITeamService.java`, `TeamService.java`, `TeamController.java`,
`dto/team/LeaveTeamRequestDTO.java` (nuovo)

Nuovo metodo `leaveTeam(teamId, userEmail, successorEmail)` con comportamento
polimorfo per ruolo:

| Chiamante | `successorEmail` | Comportamento |
|---|---|---|
| LEADER | presente | Promuove successore a LEADER, rimuove leader. Atomico. |
| LEADER | assente/null | Errore 400: "deve indicare un successore" |
| MEMBER | assente/null | Rimuove il membro. |
| MEMBER | presente | Errore 400: "solo il leader può indicare un successore" |
| LEADER unico nel team | qualsiasi | Errore 409: "usa deleteTeam per scioglierlo" |

Endpoint REST: `POST /api/teams/{teamId}/leave` → 204 No Content.

---

### Commit 5 — `feat(team): deleteTeam restricted to REGISTRATION-only registrations`

**File modificati:** `ITeamService.java`, `TeamService.java`, `TeamController.java`,
`dto/team/DeleteTeamRequestDTO.java` (nuovo), `InvitationRepository.java`

Nuovo metodo `deleteTeam(teamId, leaderEmail)`:

- Solo il leader può chiamarlo (altrimenti 403).
- Bloccato se il team è iscritto a un hackathon **non** in stato `REGISTRATION`
  (altrimenti 409). Motivazione: non si possono cancellare submission già
  valutate o lavoro in corso.
- Cascade: elimina `Invitation`, `TeamMember`, poi il `Team`.

Endpoint REST: `DELETE /api/teams/{teamId}` con body `{ "userEmail": "..." }` → 204.

---

### Commit 6 — `refactor(dto): organize DTOs by domain subpackage`

**File spostati:**

```
dto/TeamSummaryDTO.java          → dto/team/TeamSummaryDTO.java
dto/HackathonResponseDTO.java    → dto/hackathon/HackathonResponseDTO.java
dto/PrizeDisbursementResponseDTO → dto/prize/PrizeDisbursementResponseDTO.java
```

Import aggiornati in: `OrganizerServiceImpl`, `IOrganizerService`,
`OrganizerController`, `HackathonMapper`, `PrizeDisbursementMapper`.

Struttura finale del package `dto/`:

```
dto/
  team/
    TeamSummaryDTO.java
    LeaveTeamRequestDTO.java
    DeleteTeamRequestDTO.java
  hackathon/
    HackathonResponseDTO.java
  prize/
    PrizeDisbursementResponseDTO.java
```

---

### Commit 7 — `test(team): cover createTeam/leaveTeam/deleteTeam + leader-only invitation`

**File nuovi:** `TeamServiceTest.java` (18 test), `InvitationServiceTest.java` (3 test)
**File modificato:** `OrganizerServiceImplTest.java` (fix import DTO)

Test coperti:

- `createTeam`: leader assegnato, utente non trovato, nome duplicato, utente già in team.
- `leaveTeam` leader: promuove successore, successore mancante, leader unico, successore
  non membro, invariante "esattamente un leader" preservata.
- `leaveTeam` membro: rimozione, errore se passa successore, errore se non nel team.
- `deleteTeam`: successo senza registrazioni, successo con registrazione in REGISTRATION,
  non-leader bloccato, RUNNING/EVALUATION/CONCLUDED bloccati, cascade verificata.
- `sendInvitation`: successo con leader, errore con non-leader (ForbiddenOperationException).
- `acceptInvitation`: nuovo membro ha `role = MEMBER`.

---

### Commit 8 — `docs: document Team Leader role and domain constraints`

**File modificati:** `CONTEXT.md`, `docs/AGENTS.md`

- `CONTEXT.md`: aggiunta voce glossario **Team Leader** con definizione, permessi
  e anti-pattern da evitare. Aggiunte due relazioni nella sezione Relationships.
- `docs/AGENTS.md`: aggiunti 5 vincoli di dominio nella sezione Domain Constraints
  (esattamente un leader, operazioni leader-only, trasferimento atomico, leader
  unico → deleteTeam, deleteTeam bloccato su hackathon attivi).

---

## Invarianti di dominio garantite

| Invariante | Dove applicata |
|---|---|
| Esattamente un LEADER per team | `createTeam` (init), `promoteToLeader` (swap atomico) |
| Leader è anche membro | Stesso record `TeamMember` — impossibile violare |
| Utente in al massimo un team | `UNIQUE(user_id)` su `team_members` + `existsByUserId` |
| Solo il leader invita | `validateSenderIsLeader` in `InvitationService` |
| Solo il leader elimina il team | `existsByTeamIdAndUserIdAndRole(..., LEADER)` in `deleteTeam` |
| deleteTeam bloccato su hackathon attivi | check su `HackathonStatus` in `deleteTeam` |

---

## Cosa NON è stato fatto (fuori scope)

- `removeMember` (rimozione di un membro da parte del leader) — use case futuro.
- `updateTeam` (rinomina, ecc.) — spec ambigua, da chiarire.
- Aggiornamento diagrammi UML `.puml` — aggiornati manualmente in Visual Paradigm.
- Vincolo DB unique parziale su `(team_id) WHERE role = 'LEADER'` — solo livello servizio.
- Flyway/Liquibase — non in scope per fase universitaria.

---

## Nota migrazione DB

Prima del primo avvio su questa branch, cancellare il database H2 locale:

```bash
rm HackHub/data/hackhub.mv.db
rm HackHub/data/hackhub.trace.db
```

Hibernate (`ddl-auto=update`) rigenera lo schema con la nuova colonna `role` e
senza la colonna `creator_id`. I file `data/` sono gitignored e locali.
