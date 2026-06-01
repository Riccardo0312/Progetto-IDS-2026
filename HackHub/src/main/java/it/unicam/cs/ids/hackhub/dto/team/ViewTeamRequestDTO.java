package it.unicam.cs.ids.hackhub.dto.team;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO per la richiesta "Visualizza Team".
 * Contiene l’email dell’utente che vuole visualizzare il team.
 */
public record ViewTeamRequestDTO(@NotBlank @Email String userEmail) {}