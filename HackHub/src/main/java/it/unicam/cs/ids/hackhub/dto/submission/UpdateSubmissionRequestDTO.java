package it.unicam.cs.ids.hackhub.dto.submission;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

/**
 * Richiesta per la modifica di una sottomissione esistente, consentita finché
 * la deadline dell'hackathon non è superata.
 */
public record UpdateSubmissionRequestDTO(
		@NotBlank @Email String userEmail,
		@NotBlank @Size(max = 150) String title,
		@NotBlank @Size(max = 4000) String description,
		@NotBlank @URL @Size(max = 500) String projectLink) {}
