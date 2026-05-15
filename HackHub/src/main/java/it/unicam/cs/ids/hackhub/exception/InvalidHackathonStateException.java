package it.unicam.cs.ids.hackhub.exception;

import it.unicam.cs.ids.hackhub.model.HackathonStatus;

public class InvalidHackathonStateException extends RuntimeException {

	public InvalidHackathonStateException(
			Long hackathonId, HackathonStatus actualStatus, HackathonStatus expectedStatus) {
		super(
				"Hackathon "
						+ hackathonId
						+ " is in state "
						+ actualStatus
						+ " but expected "
						+ expectedStatus);
	}

}
