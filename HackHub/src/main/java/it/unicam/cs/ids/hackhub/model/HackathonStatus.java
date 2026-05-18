package it.unicam.cs.ids.hackhub.model;

import java.time.LocalDate;

public enum HackathonStatus {
	REGISTRATION,
	RUNNING,
	EVALUATION,
	CONCLUDED;

	public static HackathonStatus updateStatus(HackathonStatus currentStatus,
	                                           LocalDate registrationDeadline,
	                                           LocalDate endDate) {
		LocalDate dataCorrente = LocalDate.now();

		if (currentStatus == REGISTRATION && dataCorrente.isAfter(registrationDeadline)) {
			return RUNNING;
		}
		if (currentStatus == RUNNING && dataCorrente.isAfter(endDate)) {
			return EVALUATION;
		}
		return currentStatus;
	}
}