package it.unicam.cs.ids.hackhub.model.state.hackathon;

import it.unicam.cs.ids.hackhub.model.HackathonStatus;
import java.time.LocalDate;
import java.util.Objects;

public final class RegistrationState implements HackathonState {

	@Override
	public HackathonStatus getStatus() {
		return HackathonStatus.REGISTRATION;
	}

	@Override
	public HackathonStatus updateStatus(
			LocalDate currentDate, LocalDate registrationDeadline, LocalDate endDate) {
		Objects.requireNonNull(currentDate, "La data corrente non può essere null");

		if (registrationDeadline == null || !currentDate.isAfter(registrationDeadline)) {
			return HackathonStatus.REGISTRATION;
		}

		// Se anche la data di fine è gia superata, saltiamo direttamente
		// a EVALUATION: serve a non rimanere in RUNNING quando lo stato
		// non viene ricalcolato regolarmente.
		if (endDate != null && currentDate.isAfter(endDate)) {
			return HackathonStatus.EVALUATION;
		}

		return HackathonStatus.RUNNING;
	}

}
