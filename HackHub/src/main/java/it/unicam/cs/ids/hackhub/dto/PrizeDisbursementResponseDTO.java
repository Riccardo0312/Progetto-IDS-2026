package it.unicam.cs.ids.hackhub.dto;

import it.unicam.cs.ids.hackhub.model.PrizeDisbursementStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Risposta del caso d'uso "Eroga premio al team vincitore".
 *
 * <p>Espone l'esito dell'ultimo tentativo di erogazione registrato per
 * l'hackathon, includendo il riferimento di transazione restituito dal Sistema
 * di Pagamento esterno (in sviluppo è un identificativo fittizio).
 */
public record PrizeDisbursementResponseDTO(
		Long hackathonId,
		Long winningTeamId,
		BigDecimal amount,
		PrizeDisbursementStatus status,
		String transactionReference,
		String failureReason,
		LocalDateTime disbursedAt) {}
