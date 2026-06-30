package it.unicam.cs.ids.hackhub.service.interfaces;

import it.unicam.cs.ids.hackhub.dto.hackathon.HackathonResponseDTO;
import it.unicam.cs.ids.hackhub.dto.hackathon.UpdateHackathonRequestDTO;
import it.unicam.cs.ids.hackhub.dto.prize.PrizeDisbursementResponseDTO;
import it.unicam.cs.ids.hackhub.dto.staff.StaffMemberSummaryDTO;
import it.unicam.cs.ids.hackhub.model.Hackathon;
import it.unicam.cs.ids.hackhub.model.Submission;
import java.util.List;

public interface IOrganizerService {

	Hackathon createHackathon(Hackathon hackathon, Long organizerId,
	                          Long judgeId, List<Long> mentorIds);

	void addMentorToHackathon(Long hackathonId, Long organizerId, Long mentorId);

	void removeMentorFromHackathon(Long hackathonId, Long organizerId, Long mentorId);

	void addJudgeToHackathon(Long hackathonId, Long organizerId, Long judgeId);

	/**
	 * Sostituisce atomicamente il giudice di un hackathon. Vedi ADR 0002.
	 *
	 * <p>Permesso solo finché lo staff è ancora modificabile (REGISTRATION,
	 * READY, RUNNING). Mantiene l'invariante "esattamente un giudice" senza
	 * mai passare per uno stato intermedio "senza giudice".
	 */
	void replaceJudge(Long hackathonId, Long organizerId, Long newJudgeId);

	/**
	 * Annulla l'hackathon prima dell'inizio. Vedi ADR 0001.
	 *
	 * <p>Permesso solo in {@code REGISTRATION} o {@code READY}; le registrazioni
	 * dei team già iscritti vengono preservate (l'hackathon resta visibile come
	 * "annullato").
	 */
	void cancelHackathon(Long hackathonId, Long organizerId);

	/**
	 * Modifica i parametri descrittivi/logistici dell'hackathon (nome, regole,
	 * luogo, premio, dimensione massima del team, date). Vedi ADR 0001 e
	 * CONTEXT "Modifica dell'hackathon".
	 *
	 * <p>Permesso solo in {@code REGISTRATION}; staff (giudice, mentori) si
	 * cambia tramite gli use case dedicati, non con questa operazione.
	 */
	void updateHackathon(Long hackathonId, Long organizerId,
	                     UpdateHackathonRequestDTO request);

	List<StaffMemberSummaryDTO> getAvailableMentors();

	List<StaffMemberSummaryDTO> getAvailableJudges();

	List<StaffMemberSummaryDTO> getMentorsByHackathon(Long hackathonId, Long organizerId);

	void proclaimWinner(Long hackathonId, Long organizerId, Long teamId);

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

	/**
	 * Squalifica il team dalla partecipazione a un hackathon specifico (ADR 0005).
	 *
	 * <p>Permesso solo in {@code RUNNING} o {@code EVALUATION}. Irreversibile.
	 * Il record {@link it.unicam.cs.ids.hackhub.model.HackathonRegistration} è
	 * preservato con stato {@code DISQUALIFIED} e motivazione obbligatoria.
	 *
	 * @throws it.unicam.cs.ids.hackhub.exception.ResourceNotFoundException se il
	 *         team non è iscritto all'hackathon
	 * @throws it.unicam.cs.ids.hackhub.exception.ForbiddenOperationException se il
	 *         team è già squalificato
	 * @throws it.unicam.cs.ids.hackhub.exception.InvalidHackathonStateException se
	 *         la fase non è RUNNING o EVALUATION
	 */
	void disqualifyTeam(Long hackathonId, Long organizerId, Long teamId, String reason);

	/**
	 * Restituisce tutte le sottomissioni dei team iscritti all'hackathon, per la
	 * consultazione da parte dell'organizzatore proprietario. Include le
	 * sottomissioni dei team squalificati.
	 */
	List<Submission> getHackathonSubmissions(Long hackathonId, Long organizerId);
}
