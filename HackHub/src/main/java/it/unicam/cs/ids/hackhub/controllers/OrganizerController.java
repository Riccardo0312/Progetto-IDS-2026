package it.unicam.cs.ids.hackhub.controllers;

import it.unicam.cs.ids.hackhub.dto.hackathon.HackathonResponseDTO;
import it.unicam.cs.ids.hackhub.dto.prize.PrizeDisbursementResponseDTO;
import it.unicam.cs.ids.hackhub.service.interfaces.IOrganizerService;
import java.util.List;
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
}
