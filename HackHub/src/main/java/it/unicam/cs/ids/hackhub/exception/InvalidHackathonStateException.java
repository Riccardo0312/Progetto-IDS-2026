package it.unicam.cs.ids.hackhub.exception;

import it.unicam.cs.ids.hackhub.model.HackathonStatus;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Lanciata quando un'azione richiede uno stato dell'hackathon che non corrisponde
 * a quello corrente. Supporta uno o piu stati attesi per messaggi di errore
 * accurati quando una stessa azione e ammessa in piu fasi del ciclo di vita.
 */
public class InvalidHackathonStateException extends RuntimeException {

	public InvalidHackathonStateException(
			Long hackathonId, HackathonStatus actualStatus, HackathonStatus... expectedStatuses) {
		super(buildMessage(hackathonId, actualStatus, expectedStatuses));
	}

	private static String buildMessage(
			Long hackathonId, HackathonStatus actualStatus, HackathonStatus... expectedStatuses) {
		String expectedPart;
		if (expectedStatuses == null || expectedStatuses.length == 0) {
			expectedPart = "<none>";
		} else if (expectedStatuses.length == 1) {
			expectedPart = expectedStatuses[0].toString();
		} else {
			List<String> names =
					Arrays.stream(expectedStatuses).map(Enum::name).collect(Collectors.toList());
			expectedPart = "one of " + names;
		}
		return "Hackathon " + hackathonId
				+ " is in state " + actualStatus
				+ " but expected " + expectedPart;
	}

}
