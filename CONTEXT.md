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

**Giudice**:
A staff member assigned to an hackathon to review submitted work.
_Avoid_: Valutatore

**Mentore**:
A staff member assigned to an hackathon to support teams during the event.
_Avoid_: Tutor, Coach

**Richiesta di supporto**:
A team's request for mentoring help within a specific hackathon.
_Avoid_: Richiesta generica, ticket

**Proposta di call**:
A mentor's single scheduling proposal made in response to a support request.
_Avoid_: Prenotazione calendar, meeting

**Segnalazione di violazione**:
A mentor's report of a suspected rule violation by a team in a hackathon.
_Avoid_: Sanzione, penalita

**Sottomissione**:
The work delivered by a registered team for an hackathon.
_Avoid_: Submission when writing Italian domain notes

**Valutazione**:
The judge's written judgment and numeric score for a submitted work.
_Avoid_: Recensione, voto

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
- A **Mentore** is assigned to one or more **Hackathons**
- A **Richiesta di supporto** belongs to exactly one **Team** and exactly one **Hackathon**
- A **Mentore** handles **Richieste di supporto** only while the **Hackathon** is running
- A **Richiesta di supporto** receives at most one **Proposta di call**
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

## Example dialogue

> **Dev:** "When the hackathon is concluded, can the **Giudice** still create a **Valutazione**?"
> **Domain expert:** "No — judging happens while the hackathon is **In valutazione**; **Concluso** means the winner has already been proclaimed."

## Flagged ambiguities

- "Alla conclusione dell'hackathon" was used to describe judging timing — resolved: the **Giudice** evaluates during **In valutazione**, before the hackathon is **Concluso**.
- "Valutare una sottomissione" could mean creating or later editing a **Valutazione** — resolved: the first judging use case creates at most one **Valutazione** per **Sottomissione**.
- "Richiesta di supporto" could mean a generic team request — resolved: it belongs to a specific **Hackathon** so only assigned **Mentori** handle it.
- "Durante l'hackathon" was used to describe mentoring timing — resolved: the **Mentore** handles support only while the **Hackathon** is running.
- "Proporre una call" could allow multiple proposals for one request — resolved: each **Richiesta di supporto** receives at most one **Proposta di call**.
- "Segnalare il team" could block later reports for the same team — resolved: a **Team** can receive multiple **Segnalazioni di violazione** in the same **Hackathon**.
- "Segnalare una violazione" could happen after the event — resolved: the **Mentore** reports violations only while the **Hackathon** is running.
