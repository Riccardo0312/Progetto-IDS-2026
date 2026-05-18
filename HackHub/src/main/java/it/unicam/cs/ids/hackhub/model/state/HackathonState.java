package it.unicam.cs.ids.hackhub.model.state;

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
		permits RegistrationState, RunningState, EvaluationState, ConcludedState {

	HackathonStatus getStatus();

	default HackathonStatus updateStatus(
			LocalDate currentDate, LocalDate registrationDeadline, LocalDate endDate) {
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

}
