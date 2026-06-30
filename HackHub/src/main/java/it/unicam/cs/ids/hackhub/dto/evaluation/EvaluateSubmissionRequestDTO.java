package it.unicam.cs.ids.hackhub.dto.evaluation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Richiesta per il caso d'uso "Il giudice valuta una sottomissione".
 *
 * <p>L'identità del giudice viaggia nel path ed è validata contro il principal
 * JWT; il body porta solo giudizio e punteggio.
 */
public record EvaluateSubmissionRequestDTO(
		@NotBlank @Size(max = 2000) String judgment,
		@Min(0) @Max(10) int score) {}
