package it.unicam.cs.ids.hackhub.dto.support;

import java.time.LocalDateTime;

/**
 * DTO per la visualizzazione delle segnalazioni.
 */
public record ViolationReportDTO(
        Long id,
        String description,
        LocalDateTime reportedAt,
        String teamName,
        String mentorName
) {}