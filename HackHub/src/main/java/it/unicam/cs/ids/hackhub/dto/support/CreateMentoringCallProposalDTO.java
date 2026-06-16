package it.unicam.cs.ids.hackhub.dto.support;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateMentoringCallProposalDTO(
		@NotBlank(message = "Proposed slots must not be blank")
		@Size(max = 2000, message = "Proposed slots must not exceed 2000 characters")
		String proposedSlots) {
}
