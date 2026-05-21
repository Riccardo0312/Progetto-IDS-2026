package it.unicam.cs.ids.hackhub.dto;

import it.unicam.cs.ids.hackhub.model.HackathonStatus;
import java.math.BigDecimal;

/**
 * Vista per l'organizzatore della lista dei propri hackathon.
 */
public record HackathonResponseDTO(
		Long id,
		String name,
		HackathonStatus status,
		BigDecimal prizeMoney,
		TeamSummaryDTO winningTeam,
		boolean prizeDisbursed) {}
