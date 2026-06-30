package it.unicam.cs.ids.hackhub.dto.evaluation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Richiesta per il caso d'uso "Il giudice corregge una valutazione".
 *
 * <p>Sostituisce integralmente giudizio scritto e punteggio della valutazione
 * esistente. Vincoli speculari a {@link EvaluateSubmissionRequestDTO}.
 */
public record UpdateEvaluationRequestDTO(
		@NotBlank @Size(max = 2000) String judgment,
		@Min(0) @Max(10) int score) {}
