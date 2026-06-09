package it.unicam.cs.ids.hackhub.dto.support;

import java.time.LocalDateTime;

public record SupportRequestResponseDTO(
		Long id,
		Long hackathonId,
		Long teamId,
		String description,
		LocalDateTime requestedAt) {
}
