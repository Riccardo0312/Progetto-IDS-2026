package it.unicam.cs.ids.hackhub.model.state;

import it.unicam.cs.ids.hackhub.model.HackathonStatus;
import java.util.Objects;

/**
 * Factory che restituisce l'istanza di {@link HackathonState} associata a un
 * {@link HackathonStatus}.
 *
 * <p>Gli State sono stateless e immutabili, quindi esponiamo singleton condivisi
 * (Flyweight) invece di creare un nuovo oggetto a ogni richiesta. Questo evita
 * allocazioni inutili nei service che invocano spesso le {@code ensure*Allowed}
 * e {@code updateStatus} attraverso il Context.
 */
public final class HackathonStateFactory {

	private static final HackathonState REGISTRATION = new RegistrationState();
	private static final HackathonState RUNNING = new RunningState();
	private static final HackathonState EVALUATION = new EvaluationState();
	private static final HackathonState CONCLUDED = new ConcludedState();

	private HackathonStateFactory() {
	}

	public static HackathonState fromStatus(HackathonStatus status) {
		Objects.requireNonNull(status, "Lo stato dell'hackathon non può essere null");

		return switch (status) {
			case REGISTRATION -> REGISTRATION;
			case RUNNING -> RUNNING;
			case EVALUATION -> EVALUATION;
			case CONCLUDED -> CONCLUDED;
		};
	}

}
