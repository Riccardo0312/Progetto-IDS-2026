package it.unicam.cs.ids.hackhub.controllers;

import it.unicam.cs.ids.hackhub.dto.hackathon.HackathonResponseDTO;
import it.unicam.cs.ids.hackhub.dto.hackathon.UpdateHackathonRequestDTO;
import it.unicam.cs.ids.hackhub.dto.prize.PrizeDisbursementResponseDTO;
import it.unicam.cs.ids.hackhub.dto.staff.StaffMemberSummaryDTO;
import it.unicam.cs.ids.hackhub.service.interfaces.IOrganizerService;
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

	public OrganizerController(IOrganizerService organizerService) {
		this.organizerService = organizerService;
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

}
