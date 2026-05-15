package it.unicam.cs.ids.hackhub.service.validation;

import it.unicam.cs.ids.hackhub.exception.ForbiddenOperationException;
import it.unicam.cs.ids.hackhub.model.repository.JudgeRepository;
import org.springframework.stereotype.Component;

@Component
public class JudgeAssignedToHackathonValidator extends AbstractEvaluationRequestValidator {

	private final JudgeRepository judgeRepository;

	public JudgeAssignedToHackathonValidator(JudgeRepository judgeRepository) {
		this.judgeRepository = judgeRepository;
	}

	@Override
	protected void validateCurrent(EvaluationRequestContext context) {
		boolean judgeAssignedToHackathon =
				judgeRepository.existsByIdAndAssignedHackathonsId(context.judgeId(), context.hackathonId());

		if (!judgeAssignedToHackathon) {
			throw new ForbiddenOperationException(
					"Judge " + context.judgeId() + " is not assigned to hackathon " + context.hackathonId());
		}
	}

}
