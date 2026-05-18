package it.unicam.cs.ids.hackhub.service.impl;

import it.unicam.cs.ids.hackhub.exception.ForbiddenOperationException;
import it.unicam.cs.ids.hackhub.exception.ResourceNotFoundException;
import it.unicam.cs.ids.hackhub.model.Evaluation;
import it.unicam.cs.ids.hackhub.model.Hackathon;
import it.unicam.cs.ids.hackhub.model.Judge;
import it.unicam.cs.ids.hackhub.model.Submission;
import it.unicam.cs.ids.hackhub.model.repository.EvaluationRepository;
import it.unicam.cs.ids.hackhub.model.repository.HackathonRepository;
import it.unicam.cs.ids.hackhub.model.repository.JudgeRepository;
import it.unicam.cs.ids.hackhub.model.repository.SubmissionRepository;
import it.unicam.cs.ids.hackhub.service.interfaces.IJudgeService;
import it.unicam.cs.ids.hackhub.service.validation.EvaluationInputValidator;
import it.unicam.cs.ids.hackhub.service.validation.EvaluationRequestContext;
import it.unicam.cs.ids.hackhub.service.validation.EvaluationRequestValidator;
import it.unicam.cs.ids.hackhub.service.validation.HackathonInEvaluationValidator;
import it.unicam.cs.ids.hackhub.service.validation.JudgeAssignedToHackathonValidator;
import it.unicam.cs.ids.hackhub.service.validation.SubmissionBelongsToHackathonValidator;
import it.unicam.cs.ids.hackhub.service.validation.SubmissionNotAlreadyEvaluatedValidator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class JudgeService implements IJudgeService {

	private final JudgeRepository judgeRepository;
	private final HackathonRepository hackathonRepository;
	private final SubmissionRepository submissionRepository;
	private final EvaluationRepository evaluationRepository;
	private final EvaluationRequestValidator evaluationRequestValidator;

	public JudgeService(
			JudgeRepository judgeRepository,
			HackathonRepository hackathonRepository,
			SubmissionRepository submissionRepository,
			EvaluationRepository evaluationRepository,
			JudgeAssignedToHackathonValidator judgeAssignedToHackathonValidator,
			HackathonInEvaluationValidator hackathonInEvaluationValidator,
			SubmissionBelongsToHackathonValidator submissionBelongsToHackathonValidator,
			SubmissionNotAlreadyEvaluatedValidator submissionNotAlreadyEvaluatedValidator,
			EvaluationInputValidator evaluationInputValidator) {
		this.judgeRepository = judgeRepository;
		this.hackathonRepository = hackathonRepository;
		this.submissionRepository = submissionRepository;
		this.evaluationRepository = evaluationRepository;
		this.evaluationRequestValidator = judgeAssignedToHackathonValidator;

		judgeAssignedToHackathonValidator
				.setNext(hackathonInEvaluationValidator)
				.setNext(submissionBelongsToHackathonValidator)
				.setNext(submissionNotAlreadyEvaluatedValidator)
				.setNext(evaluationInputValidator);
	}

	@Override
	public List<Submission> getAssignedHackathonSubmissions(Long judgeId, Long hackathonId) {
		findJudgeById(judgeId);
		Hackathon hackathon = findHackathonById(hackathonId);
		validateJudgingAccess(judgeId, hackathon);

		return submissionRepository.findByRegistrationHackathonId(hackathonId);
	}

	@Override
	@Transactional
	public Evaluation evaluateSubmission(
			Long judgeId, Long hackathonId, Long submissionId, String judgment, int score) {
		Judge judge = findJudgeById(judgeId);
		Hackathon hackathon = findHackathonById(hackathonId);
		Submission submission = findSubmissionById(submissionId);
		String normalizedJudgment = normalizeJudgment(judgment);

		EvaluationRequestContext validationContext =
				new EvaluationRequestContext(
						judgeId, hackathonId, submissionId, judge, hackathon, submission, normalizedJudgment, score);

		evaluationRequestValidator.validate(validationContext);

		Evaluation evaluation = new Evaluation();
		evaluation.setJudge(judge);
		evaluation.setSubmission(submission);
		evaluation.setJudgment(normalizedJudgment);
		evaluation.setScore(score);

		Evaluation savedEvaluation = evaluationRepository.save(evaluation);
		submission.setEvaluation(savedEvaluation);
		judge.getEvaluations().add(savedEvaluation);

		return savedEvaluation;
	}

	private Judge findJudgeById(Long judgeId) {
		return judgeRepository
				.findById(judgeId)
				.orElseThrow(() -> new ResourceNotFoundException("Judge", judgeId));
	}

	private Hackathon findHackathonById(Long hackathonId) {
		Hackathon hackathon =
				hackathonRepository
						.findById(hackathonId)
						.orElseThrow(() -> new ResourceNotFoundException("Hackathon", hackathonId));
		// Allinea lo status persistito al tempo reale prima delle guardie di ruolo:
		// un hackathon con endDate gia passata deve poter essere valutato dal giudice
		// anche se nessuno ha ancora forzato il refresh.
		hackathon.updateStatus();
		return hackathon;
	}

	private Submission findSubmissionById(Long submissionId) {
		return submissionRepository
				.findById(submissionId)
				.orElseThrow(() -> new ResourceNotFoundException("Submission", submissionId));
	}

	private void validateJudgingAccess(Long judgeId, Hackathon hackathon) {
		boolean judgeAssignedToHackathon =
				judgeRepository.existsByIdAndAssignedHackathonsId(judgeId, hackathon.getId());

		if (!judgeAssignedToHackathon) {
			throw new ForbiddenOperationException(
					"Judge " + judgeId + " is not assigned to hackathon " + hackathon.getId());
		}

		hackathon.ensureJudgingActionsAllowed();
	}

	private String normalizeJudgment(String judgment) {
		return judgment == null ? null : judgment.strip();
	}

}
