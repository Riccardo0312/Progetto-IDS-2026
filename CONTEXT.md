# HackHub

HackHub is the domain context for managing hackathons, teams, staff assignments, submissions, judging, winner proclamation, and external prize payment.

## Language

**Hackathon**:
A group competition event that moves through registration, running, evaluation, and concluded phases.
_Avoid_: Hackaton

**In valutazione**:
The phase after team work ends in which assigned judges review submitted work.
_Avoid_: Alla conclusione, concluso

**Concluso**:
The phase after judging is complete and a winning team has been proclaimed.
_Avoid_: In valutazione

**Annullato**:
A terminal phase reached when the **Organizzatore** cancels an **Hackathon**
while it is still in **In iscrizione**. The record is preserved (no physical
deletion); no further state transitions are allowed. Annullato is a terminal
branch outside the four-state lifecycle (see ADR 0003), not a fifth lifecycle
phase.
_Avoid_: Eliminato, Cancellato fisicamente, Soft-deleted

**Modifica dell'hackathon**:
The act by the **Organizzatore** of changing descriptive or logistical parameters
of an **Hackathon** (name, rules, location, prizeMoney, registrationDeadline,
startDate, endDate, maxTeamSize). Allowed only while the **Hackathon** is in
**In iscrizione** and the registration deadline has not yet passed; staff
composition (Giudice, Mentori) is changed by dedicated use cases, not by this
one.
_Avoid_: Aggiornamento generico, Edit, Patch

**Sostituzione del giudice**:
An atomic operation by the **Organizzatore** that replaces the current **Giudice**
of an **Hackathon** with a different one. There is exactly one **Giudice** per
**Hackathon** at all times (except after **Annullato**); the system never allows
a transient "no judge" state. Allowed only while the **Hackathon** is in
**In iscrizione** or **In corso**.
_Avoid_: Rimozione del giudice, Cambio giudice generico

**Giudice**:
A staff member assigned to an hackathon to review submitted work.
_Avoid_: Valutatore

**Mentore**:
A staff member assigned to an hackathon to support teams during the event.
_Avoid_: Tutor, Coach

**Richiesta di supporto**:
A registered team's request for mentoring help within a specific hackathon.
_Avoid_: Richiesta generica, ticket

**Richiesta di supporto aperta**:
A support request that has not yet received a mentor follow-up.
_Avoid_: Ticket aperto, richiesta non gestita

**Risposta di supporto**:
A mentor's written answer to a support request.
_Avoid_: Commento generico, messaggio libero

**Proposta di call**:
A mentor's single scheduling proposal made in response to a support request.
_Avoid_: Prenotazione calendar, meeting

**Disponibilita proposta**:
The textual time availability or slots suggested by a mentor for a mentoring call.
_Avoid_: Evento calendar interno, prenotazione confermata

**Segnalazione di violazione**:
A mentor's report of a suspected rule violation by a team in a hackathon.
_Avoid_: Sanzione, penalita

**Squalifica del team**:
A terminal, irreversible act by the **Organizzatore** that removes a **Team**
from a single **Hackathon** because of a rule violation. Scoped to one
**Hackathon**: the **Team** keeps existing and stays in any other **Hackathon**
it joined. The **HackathonRegistration** record is preserved (no physical
deletion) and marked as disqualified, carrying a mandatory textual motivation.
Allowed only while the **Hackathon** is **In corso** or **In valutazione**. It
does not require a prior **Segnalazione di violazione** — the report is the
typical trigger, not a precondition. A disqualified **Team**: (1) can no longer
submit/update its **Sottomissione** or open **Richieste di supporto** for that
**Hackathon**; (2) is excluded from the leaderboard and cannot be proclaimed
winner; (3) disappears from the public current-view consultation — neither the
**Hackathon**'s registration list/count nor the **Team**'s "registered
hackathons" detail show that participation (see ADR 0005).
_Avoid_: Ban (it is not platform-wide), Espulsione del giocatore, Cancellazione
della registrazione (the record stays), Sanzione generica, Reintegro (no
re-instatement use case exists).

**Sottomissione**:
The work delivered by a registered team for an hackathon.
_Avoid_: Submission when writing Italian domain notes

**Valutazione**:
The judge's written judgment and numeric score for a submitted work.
_Avoid_: Recensione, voto

**Erogazione del premio**:
The act of paying the prize money to the winning **Team** of a **Concluso** hackathon, recorded by the system as an outcome (successful or failed) returned by the external Payment System.
_Avoid_: Pagamento (the gateway pays; the domain registers the disbursement), Liquidazione

**Guest**:
An unauthenticated visitor who can consult public, read-only information without
logging in: the list of **Team**s registered to an **Hackathon**, the total
registration count, and **Team** details (name, members, registered hackathons).
A **Guest** cannot access authenticated features.
_Avoid_: Utente anonimo generico, Visitatore loggato.

**Team Leader**:
A `TeamMember` with role `TeamRole.LEADER`. Each team has exactly one leader at all times.
The leader is a full team member with additional permissions: delete the team, invite new
members, and remove members. Leadership is modeled as a role on `TeamMember`, not as a
separate entity or subclass.
_Avoid_: `TeamLeader` as a class, `isLeader` as a boolean flag, `creator` as a proxy for
leadership.

**Invito**:
A request sent by a **Team Leader** to a **User** asking them to join that
**Team**. It is scoped to a single **Team** — never to an **Hackathon**: a
**User** is invited into a **Team**, not into a specific event. An **Invito**
has a status (pending, accepted, rejected). A pending **Invito** is one the
recipient has not yet acted on. A **User** consults only the **Inviti** of which
they are the recipient.
_Avoid_: Invito all'hackathon (the invite targets a Team, not an event),
Notifica (no notification system exists), Richiesta di adesione (the leader
initiates, not the user).

## Relationships

- A **Giudice** is assigned to one or more **Hackathons**
- An **Hackathon** has exactly one **Giudice** at all times (except after **Annullato**);
  the **Giudice** can be changed only via **Sostituzione del giudice**, atomically
- A **Mentore** is assigned to one or more **Hackathons**
- A **Richiesta di supporto** belongs to exactly one **Team** and exactly one **Hackathon**
- A **Team** creates **Richieste di supporto** only for **Hackathons** it is
  registered in and only while the **Hackathon** is running
- A **Team** consults only its own **Richieste di supporto** and any related
  mentor follow-up
- A **Mentore** handles **Richieste di supporto** only while the **Hackathon** is running
- A **Richiesta di supporto** receives at most one **Proposta di call**
- A **Proposta di call** includes a **Disponibilita proposta** and a booking
  link generated by the external Calendar gateway
- A **Richiesta di supporto** receives at most one **Risposta di supporto**
- A **Richiesta di supporto aperta** becomes no longer open when it receives a
  **Risposta di supporto** or a **Proposta di call**
- A **Team** can receive multiple **Segnalazioni di violazione** in the same **Hackathon**
- A **Mentore** creates **Segnalazioni di violazione** only while the **Hackathon** is running
- A **Sottomissione** belongs to exactly one **Hackathon** through a team's participation
- A **Giudice** can view only existing **Sottomissioni** for assigned **Hackathons** while they are **In valutazione**
- A **Sottomissione** receives at most one **Valutazione**
- A **Valutazione** belongs to exactly one **Sottomissione**
- An **Hackathon** becomes **Concluso** only after its **Sottomissioni** have been judged
- An **Erogazione del premio** is recorded for at most one **Concluso** hackathon
- An **Erogazione del premio** with esito positivo is final; a failed one can be retried in place
- A **Team Leader** is a **TeamMember** with role `LEADER`; exactly one per team at all times
- Only the **Team Leader** can delete the team, send invitations, or remove members
- An **Invito** belongs to exactly one **Team** and is addressed to exactly one **User** (the recipient)
- A **User** consults only the **Inviti** of which they are the recipient; a **User** already in a **Team** has no actionable (pending) **Inviti**
- A **Guest** consults, without authentication, the registrations and **Team**
  details of any **Hackathon** regardless of its phase; this consultation is
  read-only and exposes no sensitive data (no emails). The registration list is
  a **current-view** of teams still in the running: a **Team** disqualified from
  an **Hackathon** is excluded from that **Hackathon**'s registration list/count
  and from its own "registered hackathons" detail, even though the underlying
  record is preserved (see ADR 0005). Phase is not a filter — an **Annullato**
  **Hackathon** is still consultable.

## Example dialogue

> **Dev:** "When the hackathon is concluded, can the **Giudice** still create a **Valutazione**?"
> **Domain expert:** "No — judging happens while the hackathon is **In valutazione**; **Concluso** means the winner has already been proclaimed."

## Flagged ambiguities

- "Alla conclusione dell'hackathon" was used to describe judging timing — resolved: the **Giudice** evaluates during **In valutazione**, before the hackathon is **Concluso**.
- "Valutare una sottomissione" could mean creating or later editing a **Valutazione** — resolved: the first judging use case creates at most one **Valutazione** per **Sottomissione**.
- "Richiesta di supporto" could mean a generic team request — resolved: it belongs to a specific **Hackathon** so only assigned **Mentori** handle it.
- "Durante l'hackathon" was used to describe mentoring timing — resolved: the **Mentore** handles support only while the **Hackathon** is running.
- "Proporre una call" could allow multiple proposals for one request — resolved: each **Richiesta di supporto** receives at most one **Proposta di call**.
- "Calendar" could imply an internal scheduling subsystem — resolved: the
  backend delegates booking-link generation to an external Calendar gateway.
- "Rispondere a richiesta di supporto" could mean changing the original request text — resolved: the mentor creates a distinct **Risposta di supporto**.
- "Consulta le richieste di supporto" could mean the mentor queue or the team's
  own history — resolved: for the **Team**, it means consulting only its own
  **Richieste di supporto** and their mentor follow-ups.
- "Segnalare il team" could block later reports for the same team — resolved: a **Team** can receive multiple **Segnalazioni di violazione** in the same **Hackathon**.
- "Segnalare una violazione" could happen after the event — resolved: the **Mentore** reports violations only while the **Hackathon** is running.
