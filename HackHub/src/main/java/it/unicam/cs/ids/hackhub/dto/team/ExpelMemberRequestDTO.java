package it.unicam.cs.ids.hackhub.dto.team;

import jakarta.validation.constraints.NotNull;

/**
 * DTO per la richiesta "Espellere un membro dal team".
 */
public record ExpelMemberRequestDTO(
        @NotNull Long memberId
) {}