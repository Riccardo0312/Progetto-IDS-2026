package it.unicam.cs.ids.hackhub.dto.auth;

import java.util.List;

/** Utente corrente per {@code GET /api/auth/me}. Mai password. */
public record CurrentUserResponse(
		Long id,
		String name,
		String email,
		List<String> roles) {}
