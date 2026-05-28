package it.unicam.cs.ids.hackhub.model.state.hackathon;

import it.unicam.cs.ids.hackhub.model.HackathonStatus;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Stato "In attesa di inizio": le iscrizioni sono chiuse ma l'hackathon non è
 * ancora iniziato. Vedi ADR 0001 per la motivazione.
 */
public final class ReadyState implements HackathonState {

	@Override
	public HackathonStatus getStatus() {
		return HackathonStatus.READY;
	}

	@Override
	public HackathonStatus updateStatus(
			LocalDate currentDate, LocalDate registrationDeadline,
			LocalDate startDate, LocalDate endDate) {
		Objects.requireNonNull(currentDate, "La data corrente non può essere null");

		// Shortcut difensivo: se siamo già oltre endDate, salta direttamente a EVALUATION.
		if (endDate != null && currentDate.isAfter(endDate)) {
			return HackathonStatus.EVALUATION;
		}

		if (startDate != null && !currentDate.isBefore(startDate)) {
			return HackathonStatus.RUNNING;
		}

		return HackathonStatus.READY;
	}

	@Override
	public void ensureCancellationAllowed(Long hackathonId) {
		// Permesso.
	}

}
