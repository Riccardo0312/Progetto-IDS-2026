package it.unicam.cs.ids.hackhub.service.validation;

import it.unicam.cs.ids.hackhub.exception.ForbiddenOperationException;
import it.unicam.cs.ids.hackhub.model.repository.SubmissionRepository;
import org.springframework.stereotype.Component;

@Component
public class SubmissionBelongsToHackathonValidator extends AbstractEvaluationRequestValidator {

	private final SubmissionRepository submissionRepository;

	public SubmissionBelongsToHackathonValidator(SubmissionRepository submissionRepository) {
		this.submissionRepository = submissionRepository;
	}

	@Override
	protected void validateCurrent(EvaluationRequestContext context) {
		boolean submissionBelongsToHackathon =
				submissionRepository.existsByIdAndRegistrationHackathonId(
						context.submissionId(), context.hackathonId());

		if (!submissionBelongsToHackathon) {
			throw new ForbiddenOperationException(
					"Submission "
							+ context.submissionId()
							+ " does not belong to hackathon "
							+ context.hackathonId());
		}
	}

}
