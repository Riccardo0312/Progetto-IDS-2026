package it.unicam.cs.ids.hackhub.service.validation;

import it.unicam.cs.ids.hackhub.exception.InvalidHackathonStateException;
import it.unicam.cs.ids.hackhub.model.HackathonStatus;
import org.springframework.stereotype.Component;

@Component
public class HackathonInEvaluationValidator extends AbstractEvaluationRequestValidator {

	@Override
	protected void validateCurrent(EvaluationRequestContext context) {
		HackathonStatus actualStatus = context.hackathon().getStatus();

		if (actualStatus != HackathonStatus.EVALUATION) {
			throw new InvalidHackathonStateException(
					context.hackathonId(), actualStatus, HackathonStatus.EVALUATION);
		}
	}

}
