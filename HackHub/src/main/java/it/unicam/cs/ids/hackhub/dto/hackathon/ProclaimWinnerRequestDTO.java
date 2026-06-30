package it.unicam.cs.ids.hackhub.dto.hackathon;

import jakarta.validation.constraints.NotNull;

/**
 * Corpo della richiesta di proclamazione del vincitore. L'hackathon e
 * l'organizzatore viaggiano nel path; qui viaggia il team da proclamare.
 */
public record ProclaimWinnerRequestDTO(
		@NotNull Long teamId) {}
