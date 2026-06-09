package it.unicam.cs.ids.hackhub.dto.auth;

import it.unicam.cs.ids.hackhub.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Richiesta di registrazione.
 *
 * <p>Il {@code role} è il ruolo globale scelto liberamente (scelta di progetto:
 * vedi AUTHENTICATION_IMPLEMENTATION_PLAN §1.3). La password è in chiaro qui e
 * viene cifrata (BCrypt) dalla factory prima del salvataggio.
 */
public record RegisterRequest(
		@NotBlank @Size(max = 100) String name,
		@NotBlank @Email @Size(max = 150) String email,
		@NotBlank @Size(min = 8, max = 255) String password,
		@NotNull UserRole role) {}
