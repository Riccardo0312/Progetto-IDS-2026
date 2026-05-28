package it.unicam.cs.ids.hackhub.controllers;

import it.unicam.cs.ids.hackhub.dto.hackathon.HackathonResponseDTO;
import it.unicam.cs.ids.hackhub.dto.prize.PrizeDisbursementResponseDTO;
import it.unicam.cs.ids.hackhub.dto.staff.StaffMemberSummaryDTO;
import it.unicam.cs.ids.hackhub.service.interfaces.IOrganizerService;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint REST per le azioni dell'Organizzatore.
 *
 * <p>L'identificativo dell'organizzatore viaggia nel path: quando JWT verrà
 * integrato sostituirà o validerà questo parametro contro il principal.
 */
@RestController
@RequestMapping("/api/organizers/{organizerId}")
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
