package it.unicam.cs.ids.hackhub.dto.hackathon;

import it.unicam.cs.ids.hackhub.model.HackathonStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Vista sintetica di un hackathon per la lista pubblica consultabile dal
 * Visitatore. Espone solo i campi pubblici essenziali, escludendo
 * {@code rules} (testo lungo) e dati interni/staff.
 */
public record HackathonListItemDTO(
		Long id,
		String name,
		HackathonStatus status,
		LocalDate startDate,
		LocalDate endDate,
		String location,
		BigDecimal prizeMoney) {}
