package it.unicam.cs.ids.hackhub.dto.invitation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Corpo della richiesta di invito di un utente in un team. Il mittente è il
 * principal JWT (leader del team), non un campo del body.
 */
public record CreateInvitationRequestDTO(
		@NotNull Long teamId,
		@NotBlank @Email String recipientEmail) {}
