package it.unicam.cs.ids.hackhub.dto.support;

import java.time.LocalDateTime;

public record MentoringCallProposalDTO(
		Long id,
		Long supportRequestId,
		Long mentorId,
		String proposedSlots,
		String bookingLink,
		LocalDateTime proposedAt) {
}
