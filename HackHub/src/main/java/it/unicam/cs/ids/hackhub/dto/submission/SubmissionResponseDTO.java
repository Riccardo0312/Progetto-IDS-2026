package it.unicam.cs.ids.hackhub.dto.submission;

import java.time.LocalDateTime;

/**
 * Vista di una sottomissione inviata da un team.
 *
 * <p>Espone solo l'identificativo della registrazione per evitare di
 * serializzare le associazioni LAZY dell'entità.
 */
public record SubmissionResponseDTO(
		Long id,
		Long registrationId,
		String title,
		String description,
		String projectLink,
		LocalDateTime uploadedAt,
		LocalDateTime lastModifiedAt) {}
