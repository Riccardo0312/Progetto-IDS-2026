package it.unicam.cs.ids.hackhub.dto.auth;

import java.util.List;

/**
 * Risposta di registrazione/login. Mai password.
 *
 * @param token access token JWT
 * @param type  tipo token, sempre {@code "Bearer"}
 * @param id    id utente
 * @param email email utente (anche username Spring Security)
 * @param name  nome utente
 * @param roles ruoli senza prefisso {@code ROLE_} (es. {@code ["USER"]})
 */
public record AuthenticationResponse(
		String token,
		String type,
		Long id,
		String email,
		String name,
		List<String> roles) {}
