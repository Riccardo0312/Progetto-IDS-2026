package it.unicam.cs.ids.hackhub.dto.team;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Richiesta per il caso d'uso "Lascia il team".
 *
 * <p>Se {@code userEmail} appartiene al leader, {@code successorEmail} è
 * obbligatorio. Se appartiene a un membro normale, {@code successorEmail}
 * deve essere assente (null); passarlo è un errore di input.
 */
public record LeaveTeamRequestDTO(
        @NotBlank @Email String userEmail,
        @Email String successorEmail) {}
