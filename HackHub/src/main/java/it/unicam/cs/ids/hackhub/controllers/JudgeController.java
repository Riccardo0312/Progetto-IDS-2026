package it.unicam.cs.ids.hackhub.controllers;

import it.unicam.cs.ids.hackhub.dto.evaluation.EvaluateSubmissionRequestDTO;
import it.unicam.cs.ids.hackhub.dto.evaluation.EvaluationResponseDTO;
import it.unicam.cs.ids.hackhub.dto.evaluation.UpdateEvaluationRequestDTO;
import it.unicam.cs.ids.hackhub.dto.submission.SubmissionResponseDTO;
import it.unicam.cs.ids.hackhub.model.Evaluation;
import it.unicam.cs.ids.hackhub.service.interfaces.IJudgeService;
import it.unicam.cs.ids.hackhub.service.mapper.EvaluationMapper;
import it.unicam.cs.ids.hackhub.service.mapper.SubmissionMapper;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint REST per le azioni del Giudice: consultazione, valutazione e
 * correzione delle valutazioni delle sottomissioni.
 *
 * <p>L'identità del giudice viaggia nel path ({@code judgeId}) ed è validata
 * contro il principal JWT da {@code isJudgeSelf}; la verifica di assegnazione
 * all'hackathon e lo stato EVALUATION restano nel service.
 */
@RestController
@RequestMapping("/api/judges/{judgeId}")
@PreAuthorize("hasRole('JUDGE') "
		+ "and @hackHubAuthorizationService.isJudgeSelf(#judgeId, authentication.name)")
public class JudgeController {

	private final IJudgeService judgeService;
	private final SubmissionMapper submissionMapper;
	private final EvaluationMapper evaluationMapper;

	public JudgeController(
			IJudgeService judgeService,
			SubmissionMapper submissionMapper,
			EvaluationMapper evaluationMapper) {
		this.judgeService = judgeService;
		this.submissionMapper = submissionMapper;
		this.evaluationMapper = evaluationMapper;
	}

	/** Consultazione: tutte le sottomissioni dell'hackathon assegnato. */
	@GetMapping("/hackathons/{hackathonId}/submissions")
	public List<SubmissionResponseDTO> getSubmissions(
			@PathVariable Long judgeId, @PathVariable Long hackathonId) {
		return judgeService.getAssignedHackathonSubmissions(judgeId, hackathonId).stream()
				.map(submissionMapper::toResponse)
				.toList();
	}

	/** Sottomissioni già valutate dal giudice (punto di partenza della correzione). */
	@GetMapping("/hackathons/{hackathonId}/submissions/evaluated")
	public List<SubmissionResponseDTO> getEvaluatedSubmissions(
			@PathVariable Long judgeId, @PathVariable Long hackathonId) {
		return judgeService.getEvaluatedSubmissions(judgeId, hackathonId).stream()
				.map(submissionMapper::toResponse)
				.toList();
	}

	/** Valutazione: crea una nuova valutazione per la sottomissione. */
	@PostMapping("/hackathons/{hackathonId}/submissions/{submissionId}/evaluation")
	@ResponseStatus(HttpStatus.CREATED)
	public EvaluationResponseDTO evaluateSubmission(
			@PathVariable Long judgeId,
			@PathVariable Long hackathonId,
			@PathVariable Long submissionId,
			@Valid @RequestBody EvaluateSubmissionRequestDTO request) {
		Evaluation evaluation =
				judgeService.evaluateSubmission(
						judgeId, hackathonId, submissionId, request.judgment(), request.score());
		return evaluationMapper.toResponse(evaluation);
	}

	/** Correzione: sostituisce giudizio e punteggio di una valutazione esistente. */
	@PutMapping("/hackathons/{hackathonId}/submissions/{submissionId}/evaluation")
	public EvaluationResponseDTO updateEvaluation(
			@PathVariable Long judgeId,
			@PathVariable Long hackathonId,
			@PathVariable Long submissionId,
			@Valid @RequestBody UpdateEvaluationRequestDTO request) {
		Evaluation evaluation =
				judgeService.updateEvaluation(
						judgeId, hackathonId, submissionId, request.judgment(), request.score());
		return evaluationMapper.toResponse(evaluation);
	}
}
