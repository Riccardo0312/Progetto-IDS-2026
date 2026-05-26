package it.unicam.cs.ids.hackhub.dto.team;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Richiesta per il caso d'uso "Elimina team".
 * Solo il leader può eliminare il team.
 */
public record DeleteTeamRequestDTO(@NotBlank @Email String userEmail) {}
