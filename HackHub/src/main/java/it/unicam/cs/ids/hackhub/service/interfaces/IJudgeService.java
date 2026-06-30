package it.unicam.cs.ids.hackhub.service.interfaces;

import it.unicam.cs.ids.hackhub.model.Evaluation;
import it.unicam.cs.ids.hackhub.model.Submission;
import java.util.List;

public interface IJudgeService {

	List<Submission> getAssignedHackathonSubmissions(Long judgeId, Long hackathonId);

	/**
	 * Sottomissioni dell'hackathon gia valutate dal giudice indicato. Usata
	 * per presentare al giudice l'elenco delle valutazioni che puo correggere.
	 */
	List<Submission> getEvaluatedSubmissions(Long judgeId, Long hackathonId);

	Evaluation evaluateSubmission(
			Long judgeId, Long hackathonId, Long submissionId, String judgment, int score);

	/**
	 * Corregge una valutazione gia espressa dal giudice sulla sottomissione
	 * indicata, sostituendo giudizio e punteggio. Permesso solo se il giudice e
	 * assegnato all'hackathon (in stato EVALUATION) ed e l'autore della
	 * valutazione esistente.
	 */
	Evaluation updateEvaluation(
			Long judgeId, Long hackathonId, Long submissionId, String judgment, int score);

}
