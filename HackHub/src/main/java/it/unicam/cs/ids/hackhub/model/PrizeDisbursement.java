package it.unicam.cs.ids.hackhub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Registro dell'erogazione del premio in denaro per un hackathon concluso.
 *
 * <p>Vincolo di unicità su {@code hackathon_id}: esiste al più un record per
 * hackathon. Su retry dopo un esito {@link PrizeDisbursementStatus#FAILED} il
 * record viene aggiornato in-place; un esito {@link PrizeDisbursementStatus#SUCCESS}
 * è terminale (chi tenta una seconda erogazione riceve un errore di dominio).
 */
@Entity
@Table(
		name = "prize_disbursements",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_prize_disbursements_hackathon",
				columnNames = "hackathon_id"))
@Getter
@Setter
@NoArgsConstructor
public class PrizeDisbursement {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "hackathon_id", nullable = false)
	private Hackathon hackathon;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "winning_team_id", nullable = false)
	private Team winningTeam;

	@NotNull
	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal amount;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private PrizeDisbursementStatus status;

	@Column(length = 100)
	private String transactionReference;

	@Column(length = 500)
	private String failureReason;

	@NotNull
	@Column(nullable = false)
	private LocalDateTime disbursedAt;
}
