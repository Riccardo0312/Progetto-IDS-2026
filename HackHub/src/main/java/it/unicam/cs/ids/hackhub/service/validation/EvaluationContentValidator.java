package it.unicam.cs.ids.hackhub.service.validation;

import it.unicam.cs.ids.hackhub.exception.InvalidEvaluationException;
import org.springframework.stereotype.Component;

@Component
public class EvaluationContentValidator extends AbstractEvaluationRequestValidator {

	private static final int MIN_SCORE = 0;
	private static final int MAX_SCORE = 10;
	private static final int MAX_JUDGMENT_LENGTH = 2000;

	@Override
	protected void validateCurrent(EvaluationRequestContext context) {
		if (context.judgment() == null || context.judgment().isBlank()) {
			throw new InvalidEvaluationException("Evaluation judgment cannot be blank");
		}

		if (context.judgment().length() > MAX_JUDGMENT_LENGTH) {
			throw new InvalidEvaluationException(
					"Evaluation judgment cannot exceed " + MAX_JUDGMENT_LENGTH + " characters");
		}

		if (context.score() < MIN_SCORE || context.score() > MAX_SCORE) {
			throw new InvalidEvaluationException("Evaluation score must be between 0 and 10");
		}
	}

}
