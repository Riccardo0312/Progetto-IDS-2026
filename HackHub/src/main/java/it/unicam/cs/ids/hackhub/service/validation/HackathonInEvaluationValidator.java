package it.unicam.cs.ids.hackhub.service.validation;

import org.springframework.stereotype.Component;

@Component
public class HackathonInEvaluationValidator extends AbstractEvaluationRequestValidator {

	@Override
	protected void validateCurrent(EvaluationRequestContext context) {
		context.hackathon().ensureJudgingActionsAllowed();
	}

}
