package it.unicam.cs.ids.hackhub.model.state.hackathon;

import it.unicam.cs.ids.hackhub.model.HackathonStatus;
import java.time.LocalDate;
import java.util.Objects;

public final class RunningState implements HackathonState {

	@Override
	public HackathonStatus getStatus() {
		return HackathonStatus.RUNNING;
	}

	@Override
	public HackathonStatus updateStatus(
			LocalDate currentDate, LocalDate registrationDeadline, LocalDate endDate) {
		Objects.requireNonNull(currentDate, "La data corrente non può essere null");

		if (endDate != null && currentDate.isAfter(endDate)) {
			return HackathonStatus.EVALUATION;
		}
		return HackathonStatus.RUNNING;
	}

	@Override
	public void ensureMentorActionsAllowed(Long hackathonId) {
	}

}
