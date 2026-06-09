package it.unicam.cs.ids.hackhub.dto.support;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSupportResponseDTO(
		@NotBlank @Size(max = 2000) String message) {
}
