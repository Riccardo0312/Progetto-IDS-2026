package it.unicam.cs.ids.hackhub.dto.submission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

/**
 * Richiesta per il caso d'uso "Il team invia la sottomissione".
 *
 * <p>I vincoli rispecchiano quelli dell'entità {@code Submission} così da
 * fallire presto (400) prima di raggiungere il service.
 */
public record UploadSubmissionRequestDTO(
		@NotBlank @Size(max = 150) String title,
		@NotBlank @Size(max = 4000) String description,
		@NotBlank @URL @Size(max = 500) String projectLink) {}
