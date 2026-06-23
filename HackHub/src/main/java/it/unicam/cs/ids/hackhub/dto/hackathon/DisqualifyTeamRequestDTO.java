package it.unicam.cs.ids.hackhub.dto.hackathon;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DisqualifyTeamRequestDTO(
        @NotBlank @Size(max = 2000) String reason) {
}
