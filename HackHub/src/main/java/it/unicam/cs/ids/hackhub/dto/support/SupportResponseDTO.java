package it.unicam.cs.ids.hackhub.dto.support;

import java.time.LocalDateTime;

public record SupportResponseDTO(
		Long id,
		Long supportRequestId,
		Long mentorId,
		String message,
		LocalDateTime respondedAt) {
}
