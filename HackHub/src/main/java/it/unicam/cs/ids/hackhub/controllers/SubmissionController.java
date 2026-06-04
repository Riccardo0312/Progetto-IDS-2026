package it.unicam.cs.ids.hackhub.controllers;

import it.unicam.cs.ids.hackhub.dto.submission.SubmissionResponseDTO;
import it.unicam.cs.ids.hackhub.dto.submission.UpdateSubmissionRequestDTO;
import it.unicam.cs.ids.hackhub.dto.submission.UploadSubmissionRequestDTO;
import it.unicam.cs.ids.hackhub.model.Submission;
import it.unicam.cs.ids.hackhub.service.interfaces.ISubmissionService;
import it.unicam.cs.ids.hackhub.service.mapper.SubmissionMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint REST per il caso d'uso "Il team invia la sottomissione".
 *
 * <p>L'invio è ancorato alla registrazione del team all'hackathon
 * ({@code registrationId}); la modifica è ancorata alla sottomissione stessa
 * ({@code submissionId}). Entrambe le operazioni sono rifiutate dal service se
 * la deadline dell'hackathon è superata.
 */
@RestController
@RequestMapping("/api")
public class SubmissionController {

	private final ISubmissionService submissionService;
	private final SubmissionMapper submissionMapper;

	public SubmissionController(ISubmissionService submissionService, SubmissionMapper submissionMapper) {
		this.submissionService = submissionService;
		this.submissionMapper = submissionMapper;
	}

	@PostMapping("/registrations/{registrationId}/submission")
	@ResponseStatus(HttpStatus.CREATED)
	public SubmissionResponseDTO uploadSubmission(
			@PathVariable Long registrationId,
			@Valid @RequestBody UploadSubmissionRequestDTO request) {
		Submission submission = submissionService.uploadSubmission(
				registrationId, request.userEmail(), request.title(), request.description(), request.projectLink());
		return submissionMapper.toResponse(submission);
	}

	@PutMapping("/submissions/{submissionId}")
	public SubmissionResponseDTO updateSubmission(
			@PathVariable Long submissionId,
			@Valid @RequestBody UpdateSubmissionRequestDTO request) {
		Submission submission = submissionService.updateSubmission(
				submissionId, request.userEmail(), request.title(), request.description(), request.projectLink());
		return submissionMapper.toResponse(submission);
	}
}
