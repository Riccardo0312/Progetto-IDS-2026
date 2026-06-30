package it.unicam.cs.ids.hackhub.dto.support;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Corpo della richiesta di segnalazione di una violazione da parte del mentore.
 * Mentore e hackathon viaggiano nel path; qui viaggiano il team segnalato e la
 * descrizione della violazione.
 */
public record CreateViolationReportRequestDTO(
		@NotNull Long teamId,
		@NotBlank @Size(max = 2000) String description) {}
