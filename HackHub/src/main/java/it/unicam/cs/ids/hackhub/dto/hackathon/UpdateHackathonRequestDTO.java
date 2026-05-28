package it.unicam.cs.ids.hackhub.dto.hackathon;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Body della richiesta di modifica dei parametri di un hackathon. Vedi ADR
 * 0001 e CONTEXT "Modifica dell'hackathon": ammessi solo i parametri
 * descrittivi/logistici, non la composizione dello staff.
 */
public record UpdateHackathonRequestDTO(
		@NotBlank @Size(max = 150) String name,
		@NotBlank @Size(max = 4000) String rules,
		@NotBlank @Size(max = 200) String location,
		@NotNull @Min(0) BigDecimal prizeMoney,
		@Positive int maxTeamSize,
		@NotNull @FutureOrPresent LocalDate registrationDeadline,
		@NotNull LocalDate startDate,
		@NotNull LocalDate endDate) {}
