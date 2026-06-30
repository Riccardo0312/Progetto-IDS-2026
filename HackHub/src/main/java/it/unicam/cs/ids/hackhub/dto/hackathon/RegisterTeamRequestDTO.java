package it.unicam.cs.ids.hackhub.dto.hackathon;

import jakarta.validation.constraints.NotNull;

/**
 * Corpo della richiesta di iscrizione di un team a un hackathon.
 * L'hackathon è indicato nel path; qui viaggia il team da iscrivere.
 */
public record RegisterTeamRequestDTO(
        @NotNull Long teamId) {
}
