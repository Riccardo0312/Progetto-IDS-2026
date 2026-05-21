package it.unicam.cs.ids.hackhub.service.interfaces;

import it.unicam.cs.ids.hackhub.dto.HackathonResponseDTO;
import it.unicam.cs.ids.hackhub.dto.PrizeDisbursementResponseDTO;
import it.unicam.cs.ids.hackhub.model.Hackathon;
import it.unicam.cs.ids.hackhub.model.Team;
import java.util.List;

public interface IOrganizerService {

	Hackathon createHackathon(Hackathon hackathon, Long organizerId,
	                          Long judgeId, List<Long> mentorIds);

	void addMentorToHackathon(Long hackathonId, Long mentorId);

	void proclaimWinner(Long hackathonId, Team winningTeam);

	List<HackathonResponseDTO> getHackathonsByOrganizer(Long organizerId);

	/**
	 * Eroga il premio al team vincitore di un hackathon concluso.
	 *
	 * <p>Operazione idempotente rispetto a un esito {@code SUCCESS} già
	 * registrato (lancia {@code PrizeAlreadyDisbursedException}); ammette retry
	 * sovrascrivendo un eventuale esito {@code FAILED} precedente. Su esito
	 * negativo del gateway persiste il record con stato {@code FAILED} e lancia
	 * {@code PrizeDisbursementFailedException}.
	 */
	PrizeDisbursementResponseDTO disbursePrize(Long hackathonId, Long organizerId);

	/**
	 * Restituisce il registro dell'erogazione del premio per l'hackathon, se
	 * esiste un tentativo registrato.
	 */
	PrizeDisbursementResponseDTO getPrizeDisbursement(Long hackathonId, Long organizerId);
}
