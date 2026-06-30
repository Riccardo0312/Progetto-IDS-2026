package it.unicam.cs.ids.hackhub.dto.hackathon;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Body della richiesta di creazione di un hackathon.
 *
 * <p>Oltre ai parametri descrittivi/logistici (gli stessi di
 * {@link UpdateHackathonRequestDTO}) include la composizione iniziale dello
 * staff: il giudice ({@code judgeId}) e almeno un mentore ({@code mentorIds}).
 * L'organizzatore è desunto dal path, non dal body.
 */
public record CreateHackathonRequestDTO(
		@NotBlank @Size(max = 150) String name,
		@NotBlank @Size(max = 4000) String rules,
		@NotBlank @Size(max = 200) String location,
		@NotNull @Min(0) BigDecimal prizeMoney,
		@Positive int maxTeamSize,
		@NotNull @FutureOrPresent LocalDate registrationDeadline,
		@NotNull LocalDate startDate,
		@NotNull LocalDate endDate,
		@NotNull Long judgeId,
		@NotEmpty List<Long> mentorIds) {}
