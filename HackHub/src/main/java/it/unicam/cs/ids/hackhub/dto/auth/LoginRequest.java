package it.unicam.cs.ids.hackhub.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Richiesta di login con email/password. */
public record LoginRequest(
		@NotBlank @Email String email,
		@NotBlank String password) {}
