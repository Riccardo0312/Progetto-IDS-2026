package it.unicam.cs.ids.hackhub.service.validation;

import it.unicam.cs.ids.hackhub.exception.DuplicateEvaluationException;
import it.unicam.cs.ids.hackhub.model.repository.EvaluationRepository;
import org.springframework.stereotype.Component;

@Component
public class SubmissionNotAlreadyEvaluatedValidator extends AbstractEvaluationRequestValidator {

	private final EvaluationRepository evaluationRepository;

	public SubmissionNotAlreadyEvaluatedValidator(EvaluationRepository evaluationRepository) {
		this.evaluationRepository = evaluationRepository;
	}

	@Override
	protected void validateCurrent(EvaluationRequestContext context) {
		if (evaluationRepository.existsBySubmissionId(context.submissionId())) {
			throw new DuplicateEvaluationException(context.submissionId());
		}
	}

}
