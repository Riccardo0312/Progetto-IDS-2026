package it.unicam.cs.ids.hackhub.dto.hackathon;

import it.unicam.cs.ids.hackhub.dto.team.TeamSummaryDTO;
import it.unicam.cs.ids.hackhub.model.HackathonStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Vista di dettaglio pubblica di un hackathon consultabile dal Visitatore.
 * Estende i campi della lista con regolamento, scadenza iscrizioni, dimensione
 * massima del team e team vincitore (valorizzato solo quando l'hackathon è
 * concluso). Esclude sempre dati interni/staff (organizzatore, giudice,
 * mentori, iscrizioni).
 */
public record HackathonDetailDTO(
		Long id,
		String name,
		HackathonStatus status,
		LocalDate registrationDeadline,
		LocalDate startDate,
		LocalDate endDate,
		String location,
		BigDecimal prizeMoney,
		int maxTeamSize,
		String rules,
		TeamSummaryDTO winningTeam) {}
