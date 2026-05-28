package it.unicam.cs.ids.hackhub.model.state.hackathon;

import it.unicam.cs.ids.hackhub.exception.InvalidHackathonStateException;
import it.unicam.cs.ids.hackhub.model.HackathonStatus;
import java.time.LocalDate;

/**
 * Interfaccia State del ciclo di vita di un hackathon.
 *
 * <p>Sealed: l'insieme degli stati e chiuso ed esaustivo. Aggiungere uno stato
 * richiede di aggiornare {@link HackathonStateFactory}, {@link HackathonStatus}
 * e questa lista di permits in modo coordinato.
 *
 * <p>Vedi ADR 0003: il ciclo di vita prescritto dalla specifica del progetto e
 * di 4 stati (REGISTRATION, RUNNING, EVALUATION, CONCLUDED). {@code CANCELLED}
 * e uno stato terminale fuori ciclo, raggiungibile solo come eccezione
 * (annullamento prima dell'inizio).
 */
public sealed interface HackathonState
		permits RegistrationState, RunningState,
				EvaluationState, ConcludedState, CancelledState {

	HackathonStatus getStatus();

	default HackathonStatus updateStatus(
			LocalDate currentDate, LocalDate registrationDeadline,
			LocalDate startDate, LocalDate endDate) {
		return getStatus();
	}

	default void ensureMentorActionsAllowed(Long hackathonId) {
		throw new InvalidHackathonStateException(
				hackathonId, getStatus(), HackathonStatus.RUNNING);
	}

	default void ensureJudgingActionsAllowed(Long hackathonId) {
		throw new InvalidHackathonStateException(
				hackathonId, getStatus(), HackathonStatus.EVALUATION);
	}

	default void ensureWinnerProclamationAllowed(Long hackathonId) {
		throw new InvalidHackathonStateException(
				hackathonId, getStatus(), HackathonStatus.EVALUATION);
	}

	/**
	 * Default-deny: solo {@link RegistrationState} permette l'annullamento
	 * (vedi ADR 0003).
	 */
	default void ensureCancellationAllowed(Long hackathonId) {
		throw new InvalidHackathonStateException(
				hackathonId, getStatus(), HackathonStatus.REGISTRATION);
	}

}
