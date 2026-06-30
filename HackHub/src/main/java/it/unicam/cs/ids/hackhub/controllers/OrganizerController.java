package it.unicam.cs.ids.hackhub.controllers;

import it.unicam.cs.ids.hackhub.dto.hackathon.CreateHackathonRequestDTO;
import it.unicam.cs.ids.hackhub.dto.hackathon.DisqualifyTeamRequestDTO;
import it.unicam.cs.ids.hackhub.dto.hackathon.HackathonResponseDTO;
import it.unicam.cs.ids.hackhub.dto.hackathon.ProclaimWinnerRequestDTO;
import it.unicam.cs.ids.hackhub.dto.hackathon.UpdateHackathonRequestDTO;
import it.unicam.cs.ids.hackhub.dto.prize.PrizeDisbursementResponseDTO;
import it.unicam.cs.ids.hackhub.dto.staff.StaffMemberSummaryDTO;
import it.unicam.cs.ids.hackhub.dto.submission.SubmissionResponseDTO;
import it.unicam.cs.ids.hackhub.model.Hackathon;
import it.unicam.cs.ids.hackhub.service.interfaces.IOrganizerService;
import it.unicam.cs.ids.hackhub.service.mapper.HackathonMapper;
import it.unicam.cs.ids.hackhub.service.mapper.SubmissionMapper;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint REST per le azioni dell'Organizzatore.
 *
 * <p>L'identificativo dell'organizzatore viaggia nel path: quando JWT verrà
 * integrato sostituirà o validerà questo parametro contro il principal.
 */
@RestController
@RequestMapping("/api/organizers/{organizerId}")
@PreAuthorize("hasRole('ORGANIZER') "
		+ "and @hackHubAuthorizationService.isOrganizerSelf(#organizerId, authentication.name)")
public class OrganizerController {

	private final IOrganizerService organizerService;
	private final HackathonMapper hackathonMapper;
	private final SubmissionMapper submissionMapper;

	public OrganizerController(
			IOrganizerService organizerService,
			HackathonMapper hackathonMapper,
			SubmissionMapper submissionMapper) {
		this.organizerService = organizerService;
		this.hackathonMapper = hackathonMapper;
		this.submissionMapper = submissionMapper;
	}

	/**
	 * Crea un nuovo hackathon. Lo staff iniziale (giudice + almeno un mentore) è
	 * indicato nel body; l'organizzatore proprietario è il principal del path.
	 */
	@PostMapping("/hackathons")
	@ResponseStatus(HttpStatus.CREATED)
	public HackathonResponseDTO createHackathon(
			@PathVariable Long organizerId,
			@Valid @RequestBody CreateHackathonRequestDTO request) {
		Hackathon created = organizerService.createHackathon(
				hackathonMapper.toEntity(request),
				organizerId,
				request.judgeId(),
				request.mentorIds());
		return hackathonMapper.toResponse(created, false);
	}

	/**
	 * Proclama il team vincitore di un hackathon in fase EVALUATION, concludendo
	 * l'hackathon. Richiede che tutte le sottomissioni siano state valutate e che
	 * il team non sia squalificato.
	 */
	@PostMapping("/hackathons/{hackathonId}/winner")
	public HackathonResponseDTO proclaimWinner(
			@PathVariable Long organizerId,
			@PathVariable Long hackathonId,
			@Valid @RequestBody ProclaimWinnerRequestDTO request) {
		organizerService.proclaimWinner(hackathonId, organizerId, request.teamId());
		return organizerService.getHackathonsByOrganizer(organizerId).stream()
				.filter(h -> h.id().equals(hackathonId))
				.findFirst()
				.orElseThrow();
	}

	/** Consultazione: tutte le sottomissioni dei team iscritti all'hackathon. */
	@GetMapping("/hackathons/{hackathonId}/submissions")
	public List<SubmissionResponseDTO> getHackathonSubmissions(
			@PathVariable Long organizerId, @PathVariable Long hackathonId) {
		return organizerService.getHackathonSubmissions(hackathonId, organizerId).stream()
				.map(submissionMapper::toResponse)
				.toList();
	}

	@GetMapping("/hackathons")
	public List<HackathonResponseDTO> getHackathonsByOrganizer(@PathVariable Long organizerId) {
		return organizerService.getHackathonsByOrganizer(organizerId);
	}

	/**
	 * Eroga il premio al team vincitore di un hackathon concluso. Operazione
	 * idempotente: ritenta in caso di precedente esito {@code FAILED}, fallisce
	 * con 409 se l'erogazione è già avvenuta con esito {@code SUCCESS}.
	 */
	@PostMapping("/hackathons/{hackathonId}/prize-disbursement")
	public PrizeDisbursementResponseDTO disbursePrize(
			@PathVariable Long organizerId, @PathVariable Long hackathonId) {
		return organizerService.disbursePrize(hackathonId, organizerId);
	}

	@GetMapping("/hackathons/{hackathonId}/prize-disbursement")
	public PrizeDisbursementResponseDTO getPrizeDisbursement(
			@PathVariable Long organizerId, @PathVariable Long hackathonId) {
		return organizerService.getPrizeDisbursement(hackathonId, organizerId);
	}

	@PostMapping("/hackathons/{hackathonId}/mentors/{mentorId}")
	public void addMentor(
			@PathVariable Long organizerId,
			@PathVariable Long hackathonId,
			@PathVariable Long mentorId) {
		organizerService.addMentorToHackathon(hackathonId, organizerId, mentorId);
	}

	@DeleteMapping("/hackathons/{hackathonId}/mentors/{mentorId}")
	public void removeMentor(
			@PathVariable Long organizerId,
			@PathVariable Long hackathonId,
			@PathVariable Long mentorId) {
		organizerService.removeMentorFromHackathon(hackathonId, organizerId, mentorId);
	}

	@PostMapping("/hackathons/{hackathonId}/judge/{judgeId}")
	public void addJudge(
			@PathVariable Long organizerId,
			@PathVariable Long hackathonId,
			@PathVariable Long judgeId) {
		organizerService.addJudgeToHackathon(hackathonId, organizerId, judgeId);
	}

	/**
	 * Sostituisce atomicamente il giudice dell'hackathon con un nuovo giudice.
	 * Vedi ADR 0002.
	 */
	@PutMapping("/hackathons/{hackathonId}/judge/{newJudgeId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void replaceJudge(
			@PathVariable Long organizerId,
			@PathVariable Long hackathonId,
			@PathVariable Long newJudgeId) {
		organizerService.replaceJudge(hackathonId, organizerId, newJudgeId);
	}

	/**
	 * Annulla l'hackathon prima dell'inizio. Permesso solo in REGISTRATION o
	 * READY (ADR 0001). Le iscrizioni esistenti sono preservate.
	 */
	@DeleteMapping("/hackathons/{hackathonId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void cancelHackathon(
			@PathVariable Long organizerId, @PathVariable Long hackathonId) {
		organizerService.cancelHackathon(hackathonId, organizerId);
	}

	/**
	 * Modifica i parametri descrittivi/logistici dell'hackathon. Permesso solo
	 * in REGISTRATION (ADR 0001).
	 */
	@PutMapping("/hackathons/{hackathonId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateHackathon(
			@PathVariable Long organizerId,
			@PathVariable Long hackathonId,
			@Valid @RequestBody UpdateHackathonRequestDTO request) {
		organizerService.updateHackathon(hackathonId, organizerId, request);
	}


	@GetMapping("/mentors/available")
	public List<StaffMemberSummaryDTO> getAvailableMentors(@PathVariable Long organizerId) {
		return organizerService.getAvailableMentors();
	}

	@GetMapping("/judges/available")
	public List<StaffMemberSummaryDTO> getAvailableJudges(@PathVariable Long organizerId) {
		return organizerService.getAvailableJudges();
	}

	@GetMapping("/hackathons/{hackathonId}/mentors")
	public List<StaffMemberSummaryDTO> getMentorsByHackathon(
			@PathVariable Long organizerId,
			@PathVariable Long hackathonId) {
		return organizerService.getMentorsByHackathon(hackathonId, organizerId);
	}

	/**
	 * Squalifica un team da un hackathon specifico. Permesso solo in RUNNING o
	 * EVALUATION. Irreversibile (ADR 0005).
	 */
	@PostMapping("/hackathons/{hackathonId}/teams/{teamId}/disqualification")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void disqualifyTeam(
			@PathVariable Long organizerId,
			@PathVariable Long hackathonId,
			@PathVariable Long teamId,
			@Valid @RequestBody DisqualifyTeamRequestDTO request) {
		organizerService.disqualifyTeam(hackathonId, organizerId, teamId, request.reason());
	}

}
