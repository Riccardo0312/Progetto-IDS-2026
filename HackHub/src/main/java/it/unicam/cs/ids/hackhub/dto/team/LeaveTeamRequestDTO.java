package it.unicam.cs.ids.hackhub.dto.team;

import jakarta.validation.constraints.Email;

/**
 * Richiesta per il caso d'uso "Lascia il team".
 *
 * <p>L'identità di chi lascia proviene dal principal JWT, non dal body.
 * Se chi lascia è il leader, {@code successorEmail} è obbligatorio; se è un
 * membro normale deve essere assente (null). La regola è applicata dal service.
 */
public record LeaveTeamRequestDTO(
		@Email String successorEmail) {}
