# Pattern usati nel progetto

## 1. Chain of Responsibility

### Problema risolto

La valutazione di una sottomissione richiede piu controlli successivi: assegnazione del giudice, stato dell'hackathon, appartenenza della sottomissione, assenza di una valutazione precedente e validita del contenuto.

### Soluzione implementata

Il caso d'uso `JudgeService.evaluateSubmission(...)` delega questi controlli a una catena di validator. Ogni validator verifica una sola regola e passa il controllo al validator successivo.

### Classi coinvolte

- `EvaluationRequestValidator`
- `AbstractEvaluationRequestValidator`
- `EvaluationRequestContext`
- `JudgeAssignedToHackathonValidator`
- `HackathonInEvaluationValidator`
- `SubmissionBelongsToHackathonValidator`
- `SubmissionNotAlreadyEvaluatedValidator`
- `EvaluationContentValidator`
- `JudgeService`

### Perché questo pattern è adatto

Il pattern evita un unico metodo di servizio pieno di condizioni e rende ogni regola indipendente, testabile e modificabile senza cambiare l'intero flusso di valutazione.

### Alternative considerate

Una soluzione semplice con controlli `if` direttamente in `JudgeService` sarebbe sufficiente per poche regole, ma diventerebbe meno leggibile man mano che aumentano i controlli autorizzativi e di validazione.

`Strategy` non e stata scelta per questa implementazione perche il punteggio viene inserito manualmente dal giudice e non esistono ancora algoritmi intercambiabili di scoring.

### Vantaggi

- maggiore estendibilità;
- minore accoppiamento tra regole di validazione;
- codice piu testabile;
- service layer piu leggibile.

### Svantaggi o compromessi

- maggiore numero di classi;
- maggiore complessita iniziale rispetto a controlli diretti nel service.
