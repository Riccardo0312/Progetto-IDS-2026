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

**Sottomissione**:
The work delivered by a registered team for an hackathon.
_Avoid_: Submission when writing Italian domain notes

**Valutazione**:
The judge's written judgment and numeric score for a submitted work.
_Avoid_: Recensione, voto

**Punteggio finale**:
The numeric result assigned to a **Sottomissione** by the single **Giudice** of an **Hackathon**.
_Avoid_: Media dei voti, punteggio calcolato separatamente

**Classifica finale**:
The ordered ranking of eligible teams in an **Hackathon**, based on each team's **Punteggio finale**.
_Avoid_: Classifica parziale, leaderboard provvisoria

**Erogazione del premio**:
The act of paying the prize money to the winning **Team** of a **Concluso** hackathon, recorded by the system as an outcome (successful or failed) returned by the external Payment System.
_Avoid_: Pagamento (the gateway pays; the domain registers the disbursement), Liquidazione

**Team Leader**:
A `TeamMember` with role `TeamRole.LEADER`. Each team has exactly one leader at all times.
The leader is a full team member with additional permissions: delete the team, invite new
members, and remove members. Leadership is modeled as a role on `TeamMember`, not as a
separate entity or subclass.
_Avoid_: `TeamLeader` as a class, `isLeader` as a boolean flag, `creator` as a proxy for
leadership.

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
- The **Segnalazioni di violazione** of an **Hackathon** can be viewed by its
  **Organizzatore**, its assigned **Giudice**, its assigned **Mentori**, and the
  **Team** named in each **Segnalazione di violazione**
- A **Sottomissione** belongs to exactly one **Hackathon** through a team's participation
- A **Giudice** can view only existing **Sottomissioni** for assigned **Hackathons** while they are **In valutazione**
- A **Sottomissione** receives at most one **Valutazione**
- A **Valutazione** belongs to exactly one **Sottomissione**
- A **Valutazione** determines exactly one **Punteggio finale** because an
  **Hackathon** has exactly one **Giudice**
- A **Classifica finale** includes only eligible teams whose **Sottomissione**
  has a **Valutazione**
- An **Hackathon** becomes **Concluso** only after its **Sottomissioni** have been judged
- An **Erogazione del premio** is recorded for at most one **Concluso** hackathon
- An **Erogazione del premio** with esito positivo is final; a failed one can be retried in place
- A **Team Leader** is a **TeamMember** with role `LEADER`; exactly one per team at all times
- Only the **Team Leader** can delete the team, send invitations, or remove members

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
- "Team interessato" could mean any team registered to the **Hackathon** —
  resolved: it means the **Team** named in that **Segnalazione di violazione**.
- "Calcolare il punteggio finale" could imply a separate aggregation step —
  resolved: with exactly one **Giudice** per **Hackathon**, the numeric score in
  the **Valutazione** is already the **Punteggio finale**.
