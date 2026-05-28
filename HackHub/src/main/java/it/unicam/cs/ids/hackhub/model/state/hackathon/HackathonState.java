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
 */
public sealed interface HackathonState
		permits RegistrationState, ReadyState, RunningState,
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
	 * Default-deny: solo {@link RegistrationState} e {@link ReadyState} permettono
	 * l'annullamento (vedi ADR 0001).
	 */
	default void ensureCancellationAllowed(Long hackathonId) {
		throw new InvalidHackathonStateException(
				hackathonId, getStatus(),
				HackathonStatus.REGISTRATION, HackathonStatus.READY);
	}

	/**
	 * Default-deny: solo {@link RegistrationState} permette la modifica dei
	 * parametri (vedi ADR 0001).
	 */
	default void ensureModificationAllowed(Long hackathonId) {
		throw new InvalidHackathonStateException(
				hackathonId, getStatus(), HackathonStatus.REGISTRATION);
	}

}
