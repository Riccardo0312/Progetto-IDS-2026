package it.unicam.cs.ids.hackhub.dto.evaluation;

/**
 * Vista di una valutazione espressa da un giudice su una sottomissione.
 *
 * <p>Espone solo gli identificativi delle associazioni per evitare di
 * serializzare le relazioni LAZY dell'entità.
 */
public record EvaluationResponseDTO(
		Long id,
		Long submissionId,
		Long judgeId,
		String judgment,
		int score) {}
