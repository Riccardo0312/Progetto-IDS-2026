package it.unicam.cs.ids.hackhub.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.unicam.cs.ids.hackhub.dto.prize.PrizeDisbursementResponseDTO;
import it.unicam.cs.ids.hackhub.exception.ForbiddenOperationException;
import it.unicam.cs.ids.hackhub.exception.InvalidHackathonStateException;
import it.unicam.cs.ids.hackhub.exception.PrizeAlreadyDisbursedException;
import it.unicam.cs.ids.hackhub.exception.PrizeDisbursementFailedException;
import it.unicam.cs.ids.hackhub.exception.ResourceNotFoundException;
import it.unicam.cs.ids.hackhub.model.Evaluation;
import it.unicam.cs.ids.hackhub.model.Hackathon;
import it.unicam.cs.ids.hackhub.model.HackathonRegistration;
import it.unicam.cs.ids.hackhub.model.HackathonStatus;
import it.unicam.cs.ids.hackhub.model.Organizer;
import it.unicam.cs.ids.hackhub.model.PaymentResult;
import it.unicam.cs.ids.hackhub.model.PrizeDisbursement;
import it.unicam.cs.ids.hackhub.model.PrizeDisbursementStatus;
import it.unicam.cs.ids.hackhub.model.Submission;
import it.unicam.cs.ids.hackhub.model.Team;
import it.unicam.cs.ids.hackhub.model.repository.HackathonRepository;
import it.unicam.cs.ids.hackhub.model.repository.JudgeRepository;
import it.unicam.cs.ids.hackhub.model.repository.MentorRepository;
import it.unicam.cs.ids.hackhub.model.repository.OrganizerRepository;
import it.unicam.cs.ids.hackhub.model.repository.PrizeDisbursementRepository;
import it.unicam.cs.ids.hackhub.service.interfaces.IPaymentGateway;
import it.unicam.cs.ids.hackhub.service.mapper.HackathonMapper;
import it.unicam.cs.ids.hackhub.service.mapper.PrizeDisbursementMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrganizerServiceImplTest {

	private static final Long HACKATHON_ID = 10L;
	private static final Long ORGANIZER_ID = 99L;
	private static final Long WINNING_TEAM_ID = 100L;

	@Mock private HackathonRepository hackathonRepository;
	@Mock private OrganizerRepository organizerRepository;
	@Mock private JudgeRepository judgeRepository;
	@Mock private MentorRepository mentorRepository;
	@Mock private PrizeDisbursementRepository prizeDisbursementRepository;
	@Mock private IPaymentGateway paymentGateway;
	@Mock private HackathonMapper hackathonMapper;
	@Mock private PrizeDisbursementMapper prizeDisbursementMapper;

	private OrganizerServiceImpl organizerService;

	@BeforeEach
	void setUp() {
		organizerService = new OrganizerServiceImpl(
				hackathonRepository,
				organizerRepository,
				judgeRepository,
				mentorRepository,
				prizeDisbursementRepository,
				paymentGateway,
				hackathonMapper,
				prizeDisbursementMapper);
	}

	// ---- proclaimWinner ----

	@Test
	void proclaimWinnerConcludesEvaluationHackathonWithoutPaying() {
		Hackathon hackathon = createHackathon(HackathonStatus.EVALUATION);
		Team winningTeam = hackathon.getRegistrations().getFirst().getTeam();

		when(hackathonRepository.findById(HACKATHON_ID)).thenReturn(Optional.of(hackathon));
		when(hackathonRepository.save(hackathon)).thenReturn(hackathon);

		organizerService.proclaimWinner(HACKATHON_ID, winningTeam);

		assertThat(hackathon.getWinningTeam()).isSameAs(winningTeam);
		assertThat(hackathon.getStatus()).isEqualTo(HackathonStatus.CONCLUDED);
		// Erogazione disaccoppiata: proclaimWinner non deve toccare il gateway.
		verify(paymentGateway, never()).payPrize(any(), any(), any());
	}

	@Test
	void proclaimWinnerRejectsHackathonsOutsideEvaluation() {
		Hackathon hackathon = createHackathon(HackathonStatus.RUNNING);
		Team winningTeam = new Team();

		when(hackathonRepository.findById(HACKATHON_ID)).thenReturn(Optional.of(hackathon));

		assertThatThrownBy(() -> organizerService.proclaimWinner(HACKATHON_ID, winningTeam))
				.isInstanceOf(InvalidHackathonStateException.class);

		verify(hackathonRepository, never()).save(any());
		verify(paymentGateway, never()).payPrize(any(), any(), any());
	}

	@Test
	void proclaimWinnerRejectsEvaluationHackathonWithPendingSubmissions() {
		Hackathon hackathon = createHackathon(HackathonStatus.EVALUATION);
		hackathon.getRegistrations().getFirst().getSubmission().setEvaluation(null);
		Team winningTeam = hackathon.getRegistrations().getFirst().getTeam();

		when(hackathonRepository.findById(HACKATHON_ID)).thenReturn(Optional.of(hackathon));

		assertThatThrownBy(() -> organizerService.proclaimWinner(HACKATHON_ID, winningTeam))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("Ci sono ancora sottomissioni non valutate");

		verify(hackathonRepository, never()).save(any());
		verify(paymentGateway, never()).payPrize(any(), any(), any());
	}

	// ---- disbursePrize ----

	@Test
	void disbursePrizePersistsSuccessOnFirstAttempt() {
		Hackathon hackathon = createConcludedHackathon();
		PaymentResult gatewayResult = PaymentResult.success("FAKE-TX-1");
		PrizeDisbursementResponseDTO expectedResponse = sampleResponse(PrizeDisbursementStatus.SUCCESS);

		when(hackathonRepository.findById(HACKATHON_ID)).thenReturn(Optional.of(hackathon));
		when(prizeDisbursementRepository.findByHackathonId(HACKATHON_ID))
				.thenReturn(Optional.empty());
		when(paymentGateway.payPrize(eq(hackathon), any(Team.class), eq(hackathon.getPrizeMoney())))
				.thenReturn(gatewayResult);
		when(prizeDisbursementRepository.save(any(PrizeDisbursement.class)))
				.thenAnswer(inv -> inv.getArgument(0));
		when(prizeDisbursementMapper.toResponse(any(PrizeDisbursement.class)))
				.thenReturn(expectedResponse);

		PrizeDisbursementResponseDTO result =
				organizerService.disbursePrize(HACKATHON_ID, ORGANIZER_ID);

		assertThat(result).isSameAs(expectedResponse);
		ArgumentCaptor<PrizeDisbursement> captor = ArgumentCaptor.forClass(PrizeDisbursement.class);
		verify(prizeDisbursementRepository).save(captor.capture());
		PrizeDisbursement saved = captor.getValue();
		assertThat(saved.getStatus()).isEqualTo(PrizeDisbursementStatus.SUCCESS);
		assertThat(saved.getTransactionReference()).isEqualTo("FAKE-TX-1");
		assertThat(saved.getFailureReason()).isNull();
		assertThat(saved.getAmount()).isEqualTo(hackathon.getPrizeMoney());
		assertThat(saved.getHackathon()).isSameAs(hackathon);
		assertThat(saved.getWinningTeam()).isSameAs(hackathon.getWinningTeam());
	}

	@Test
	void disbursePrizeRejectsNonConcludedHackathon() {
		Hackathon hackathon = createHackathon(HackathonStatus.EVALUATION);
		hackathon.setOrganizer(organizerWithId(ORGANIZER_ID));

		when(hackathonRepository.findById(HACKATHON_ID)).thenReturn(Optional.of(hackathon));

		assertThatThrownBy(() -> organizerService.disbursePrize(HACKATHON_ID, ORGANIZER_ID))
				.isInstanceOf(InvalidHackathonStateException.class);

		verify(paymentGateway, never()).payPrize(any(), any(), any());
		verify(prizeDisbursementRepository, never()).save(any());
	}

	@Test
	void disbursePrizeRejectsWrongOrganizer() {
		Hackathon hackathon = createConcludedHackathon();
		Long wrongOrganizerId = ORGANIZER_ID + 1;

		when(hackathonRepository.findById(HACKATHON_ID)).thenReturn(Optional.of(hackathon));

		assertThatThrownBy(() -> organizerService.disbursePrize(HACKATHON_ID, wrongOrganizerId))
				.isInstanceOf(ForbiddenOperationException.class);

		verify(paymentGateway, never()).payPrize(any(), any(), any());
		verify(prizeDisbursementRepository, never()).save(any());
	}

	@Test
	void disbursePrizeRejectsSecondAttemptAfterSuccess() {
		Hackathon hackathon = createConcludedHackathon();
		PrizeDisbursement existing = new PrizeDisbursement();
		existing.setStatus(PrizeDisbursementStatus.SUCCESS);
		existing.setHackathon(hackathon);
		existing.setWinningTeam(hackathon.getWinningTeam());
		existing.setAmount(hackathon.getPrizeMoney());
		existing.setTransactionReference("FAKE-TX-OLD");
		existing.setDisbursedAt(LocalDateTime.now().minusDays(1));

		when(hackathonRepository.findById(HACKATHON_ID)).thenReturn(Optional.of(hackathon));
		when(prizeDisbursementRepository.findByHackathonId(HACKATHON_ID))
				.thenReturn(Optional.of(existing));

		assertThatThrownBy(() -> organizerService.disbursePrize(HACKATHON_ID, ORGANIZER_ID))
				.isInstanceOf(PrizeAlreadyDisbursedException.class);

		verify(paymentGateway, never()).payPrize(any(), any(), any());
		verify(prizeDisbursementRepository, never()).save(any());
	}

	@Test
	void disbursePrizeRetriesAfterPreviousFailureAndOverwritesRecord() {
		Hackathon hackathon = createConcludedHackathon();
		PrizeDisbursement failed = new PrizeDisbursement();
		failed.setId(1L);
		failed.setStatus(PrizeDisbursementStatus.FAILED);
		failed.setHackathon(hackathon);
		failed.setWinningTeam(hackathon.getWinningTeam());
		failed.setAmount(hackathon.getPrizeMoney());
		failed.setFailureReason("temporary error");
		failed.setDisbursedAt(LocalDateTime.now().minusHours(1));

		PaymentResult gatewayResult = PaymentResult.success("FAKE-TX-RETRY");
		PrizeDisbursementResponseDTO expectedResponse = sampleResponse(PrizeDisbursementStatus.SUCCESS);

		when(hackathonRepository.findById(HACKATHON_ID)).thenReturn(Optional.of(hackathon));
		when(prizeDisbursementRepository.findByHackathonId(HACKATHON_ID))
				.thenReturn(Optional.of(failed));
		when(paymentGateway.payPrize(eq(hackathon), any(Team.class), eq(hackathon.getPrizeMoney())))
				.thenReturn(gatewayResult);
		when(prizeDisbursementRepository.save(any(PrizeDisbursement.class)))
				.thenAnswer(inv -> inv.getArgument(0));
		when(prizeDisbursementMapper.toResponse(any(PrizeDisbursement.class)))
				.thenReturn(expectedResponse);

		PrizeDisbursementResponseDTO result =
				organizerService.disbursePrize(HACKATHON_ID, ORGANIZER_ID);

		assertThat(result).isSameAs(expectedResponse);
		ArgumentCaptor<PrizeDisbursement> captor = ArgumentCaptor.forClass(PrizeDisbursement.class);
		verify(prizeDisbursementRepository, times(1)).save(captor.capture());
		PrizeDisbursement saved = captor.getValue();
		// Stesso record, sovrascritto in-place.
		assertThat(saved).isSameAs(failed);
		assertThat(saved.getStatus()).isEqualTo(PrizeDisbursementStatus.SUCCESS);
		assertThat(saved.getTransactionReference()).isEqualTo("FAKE-TX-RETRY");
		assertThat(saved.getFailureReason()).isNull();
	}

	@Test
	void disbursePrizePersistsFailedRecordAndThrowsWhenGatewayFails() {
		Hackathon hackathon = createConcludedHackathon();
		PaymentResult gatewayResult = PaymentResult.failure("provider unavailable");

		when(hackathonRepository.findById(HACKATHON_ID)).thenReturn(Optional.of(hackathon));
		when(prizeDisbursementRepository.findByHackathonId(HACKATHON_ID))
				.thenReturn(Optional.empty());
		when(paymentGateway.payPrize(eq(hackathon), any(Team.class), eq(hackathon.getPrizeMoney())))
				.thenReturn(gatewayResult);
		when(prizeDisbursementRepository.save(any(PrizeDisbursement.class)))
				.thenAnswer(inv -> inv.getArgument(0));

		assertThatThrownBy(() -> organizerService.disbursePrize(HACKATHON_ID, ORGANIZER_ID))
				.isInstanceOf(PrizeDisbursementFailedException.class)
				.hasMessageContaining("provider unavailable");

		ArgumentCaptor<PrizeDisbursement> captor = ArgumentCaptor.forClass(PrizeDisbursement.class);
		verify(prizeDisbursementRepository).save(captor.capture());
		PrizeDisbursement saved = captor.getValue();
		assertThat(saved.getStatus()).isEqualTo(PrizeDisbursementStatus.FAILED);
		assertThat(saved.getFailureReason()).isEqualTo("provider unavailable");
		assertThat(saved.getTransactionReference()).isNull();
	}

	@Test
	void disbursePrizeFailsWhenHackathonNotFound() {
		when(hackathonRepository.findById(HACKATHON_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> organizerService.disbursePrize(HACKATHON_ID, ORGANIZER_ID))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	// ---- helpers ----

	private Hackathon createConcludedHackathon() {
		Hackathon hackathon = createHackathon(HackathonStatus.EVALUATION);
		hackathon.setOrganizer(organizerWithId(ORGANIZER_ID));
		Team winningTeam = hackathon.getRegistrations().getFirst().getTeam();
		hackathon.concludeWith(winningTeam);
		return hackathon;
	}

	private Organizer organizerWithId(Long id) {
		Organizer organizer = new Organizer();
		organizer.setId(id);
		return organizer;
	}

	private PrizeDisbursementResponseDTO sampleResponse(PrizeDisbursementStatus status) {
		return new PrizeDisbursementResponseDTO(
				HACKATHON_ID,
				WINNING_TEAM_ID,
				BigDecimal.valueOf(1000),
				status,
				"FAKE-TX-1",
				null,
				LocalDateTime.now());
	}

	private Hackathon createHackathon(HackathonStatus status) {
		Hackathon hackathon = new Hackathon();
		hackathon.setId(HACKATHON_ID);
		hackathon.setPrizeMoney(BigDecimal.valueOf(1000));
		hackathon.getRegistrations().add(createEvaluatedRegistration());
		applyStatus(hackathon, status);
		return hackathon;
	}

	private void applyStatus(Hackathon hackathon, HackathonStatus targetStatus) {
		LocalDate today = LocalDate.of(2026, 5, 18);
		switch (targetStatus) {
			case REGISTRATION -> {
				hackathon.setRegistrationDeadline(today.plusDays(5));
				hackathon.setEndDate(today.plusDays(15));
				hackathon.updateStatus(today);
			}
			case RUNNING -> {
				hackathon.setRegistrationDeadline(today.minusDays(2));
				hackathon.setEndDate(today.plusDays(5));
				hackathon.updateStatus(today);
			}
			case EVALUATION -> {
				hackathon.setRegistrationDeadline(today.minusDays(10));
				hackathon.setEndDate(today.minusDays(2));
				hackathon.updateStatus(today);
			}
			case CONCLUDED -> throw new IllegalArgumentException(
					"CONCLUDED non è raggiungibile via updateStatus: usare concludeWith(team)");
		}
	}

	private HackathonRegistration createEvaluatedRegistration() {
		Submission submission = new Submission();
		submission.setEvaluation(new Evaluation());
		Team team = new Team();
		team.setId(WINNING_TEAM_ID);

		HackathonRegistration registration = new HackathonRegistration();
		registration.setSubmission(submission);
		registration.setTeam(team);
		return registration;
	}
}
