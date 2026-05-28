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
			LocalDate currentDate, LocalDate registrationDeadline,
			LocalDate startDate, LocalDate endDate) {
		Objects.requireNonNull(currentDate, "La data corrente non può essere null");

		// Shortcut difensivo: se siamo già oltre endDate, salta direttamente a
		// EVALUATION. Utile se updateStatus non viene chiamata regolarmente.
		if (endDate != null && currentDate.isAfter(endDate)) {
			return HackathonStatus.EVALUATION;
		}

		// La transizione automatica avviene su startDate, non su
		// registrationDeadline. RUNNING significa "evento in corso".
		// Le iscrizioni sono "chiuse" durante REGISTRATION dopo
		// registrationDeadline ma e una sotto-fase, non uno stato proprio:
		// e gestita dal check applicativo Hackathon.ensureNewRegistrationsAllowed.
		// Vedi ADR 0003.
		if (startDate != null && !currentDate.isBefore(startDate)) {
			return HackathonStatus.RUNNING;
		}

		return HackathonStatus.REGISTRATION;
	}

	@Override
	public void ensureCancellationAllowed(Long hackathonId) {
		// Permesso.
	}

}
