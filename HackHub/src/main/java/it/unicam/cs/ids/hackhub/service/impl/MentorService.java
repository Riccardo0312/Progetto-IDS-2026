package it.unicam.cs.ids.hackhub.service.impl;

import it.unicam.cs.ids.hackhub.exception.ForbiddenOperationException;
import it.unicam.cs.ids.hackhub.exception.ResourceNotFoundException;
import it.unicam.cs.ids.hackhub.model.Hackathon;
import it.unicam.cs.ids.hackhub.model.Mentor;
import it.unicam.cs.ids.hackhub.model.MentoringCallProposal;
import it.unicam.cs.ids.hackhub.model.SupportRequest;
import it.unicam.cs.ids.hackhub.model.Team;
import it.unicam.cs.ids.hackhub.model.ViolationReport;
import it.unicam.cs.ids.hackhub.model.repository.HackathonRegistrationRepository;
import it.unicam.cs.ids.hackhub.model.repository.HackathonRepository;
import it.unicam.cs.ids.hackhub.model.repository.MentorRepository;
import it.unicam.cs.ids.hackhub.model.repository.MentoringCallProposalRepository;
import it.unicam.cs.ids.hackhub.model.repository.SupportRequestRepository;
import it.unicam.cs.ids.hackhub.model.repository.TeamRepository;
import it.unicam.cs.ids.hackhub.model.repository.ViolationReportRepository;
import it.unicam.cs.ids.hackhub.service.interfaces.CalendarGateway;
import it.unicam.cs.ids.hackhub.service.interfaces.IMentorService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MentorService implements IMentorService {

	private final MentorRepository mentorRepository;
	private final HackathonRepository hackathonRepository;
	private final SupportRequestRepository supportRequestRepository;
	private final MentoringCallProposalRepository mentoringCallProposalRepository;
	private final TeamRepository teamRepository;
	private final HackathonRegistrationRepository hackathonRegistrationRepository;
	private final ViolationReportRepository violationReportRepository;
	private final CalendarGateway calendarGateway;

	public MentorService(
			MentorRepository mentorRepository,
			HackathonRepository hackathonRepository,
			SupportRequestRepository supportRequestRepository,
			MentoringCallProposalRepository mentoringCallProposalRepository,
			TeamRepository teamRepository,
			HackathonRegistrationRepository hackathonRegistrationRepository,
			ViolationReportRepository violationReportRepository,
			CalendarGateway calendarGateway) {
		this.mentorRepository = mentorRepository;
		this.hackathonRepository = hackathonRepository;
		this.supportRequestRepository = supportRequestRepository;
		this.mentoringCallProposalRepository = mentoringCallProposalRepository;
		this.teamRepository = teamRepository;
		this.hackathonRegistrationRepository = hackathonRegistrationRepository;
		this.violationReportRepository = violationReportRepository;
		this.calendarGateway = calendarGateway;
	}

	@Override
	public List<SupportRequest> getAssignedHackathonSupportRequests(
			Long mentorId, Long hackathonId) {
		findMentorById(mentorId);
		Hackathon hackathon = findHackathonById(hackathonId);
		validateMentorCanActOnRunningHackathon(mentorId, hackathon);

		return supportRequestRepository.findByHackathonId(hackathonId);
	}

	@Override
	@Transactional
	public MentoringCallProposal proposeCall(Long mentorId, Long hackathonId, Long supportRequestId) {
		Mentor mentor = findMentorById(mentorId);
		Hackathon hackathon = findHackathonById(hackathonId);
		SupportRequest supportRequest = findSupportRequestById(supportRequestId);

		validateMentorCanActOnRunningHackathon(mentorId, hackathon);
		validateSupportRequestBelongsToHackathon(supportRequest, hackathonId);
		validateSupportRequestHasNoCallProposal(supportRequestId);

		String bookingLink = calendarGateway.createBookingLink(supportRequest, mentor);
		validateBookingLink(bookingLink);

		MentoringCallProposal callProposal = new MentoringCallProposal();
		callProposal.setMentor(mentor);
		callProposal.setSupportRequest(supportRequest);
		callProposal.setBookingLink(bookingLink.strip());
		callProposal.setProposedAt(LocalDateTime.now());

		MentoringCallProposal savedCallProposal = mentoringCallProposalRepository.save(callProposal);
		supportRequest.setCallProposal(savedCallProposal);
		mentor.getCallProposals().add(savedCallProposal);

		return savedCallProposal;
	}

	@Override
	@Transactional
	public ViolationReport reportViolation(
			Long mentorId, Long hackathonId, Long teamId, String description) {
		Mentor mentor = findMentorById(mentorId);
		Hackathon hackathon = findHackathonById(hackathonId);
		Team team = findTeamById(teamId);
		String normalizedDescription = normalizeRequiredDescription(description);

		validateMentorCanActOnRunningHackathon(mentorId, hackathon);
		validateTeamRegisteredForHackathon(hackathonId, teamId);

		ViolationReport violationReport = new ViolationReport();
		violationReport.setMentor(mentor);
		violationReport.setHackathon(hackathon);
		violationReport.setTeam(team);
		violationReport.setDescription(normalizedDescription);
		violationReport.setReportedAt(LocalDateTime.now());

		ViolationReport savedViolationReport = violationReportRepository.save(violationReport);
		mentor.getViolationReports().add(savedViolationReport);
		team.getViolationReports().add(savedViolationReport);
		hackathon.getViolationReports().add(savedViolationReport);

		return savedViolationReport;
	}

	private Mentor findMentorById(Long mentorId) {
		return mentorRepository
				.findById(mentorId)
				.orElseThrow(() -> new ResourceNotFoundException("Mentor", mentorId));
	}

	private Hackathon findHackathonById(Long hackathonId) {
		Hackathon hackathon =
				hackathonRepository
						.findById(hackathonId)
						.orElseThrow(() -> new ResourceNotFoundException("Hackathon", hackathonId));
		// Allinea lo status persistito al tempo reale prima di applicare le guardie:
		// evita che un hackathon con endDate gia passata risulti ancora RUNNING per il client.
		hackathon.updateStatus();
		return hackathon;
	}

	private SupportRequest findSupportRequestById(Long supportRequestId) {
		return supportRequestRepository
				.findById(supportRequestId)
				.orElseThrow(() -> new ResourceNotFoundException("SupportRequest", supportRequestId));
	}

	private Team findTeamById(Long teamId) {
		return teamRepository
				.findById(teamId)
				.orElseThrow(() -> new ResourceNotFoundException("Team", teamId));
	}

	private void validateMentorCanActOnRunningHackathon(Long mentorId, Hackathon hackathon) {
		boolean mentorAssignedToHackathon =
				mentorRepository.existsByIdAndSupportedHackathonsId(mentorId, hackathon.getId());

		if (!mentorAssignedToHackathon) {
			throw new ForbiddenOperationException(
					"Mentor " + mentorId + " is not assigned to hackathon " + hackathon.getId());
		}

		hackathon.ensureMentorActionsAllowed();
	}

	private void validateSupportRequestBelongsToHackathon(
			SupportRequest supportRequest, Long hackathonId) {
		Hackathon supportRequestHackathon = supportRequest.getHackathon();

		if (supportRequestHackathon == null
				|| !Objects.equals(supportRequestHackathon.getId(), hackathonId)) {
			throw new ForbiddenOperationException(
					"SupportRequest " + supportRequest.getId() + " does not belong to hackathon "
							+ hackathonId);
		}
	}

	private void validateSupportRequestHasNoCallProposal(Long supportRequestId) {
		if (mentoringCallProposalRepository.existsBySupportRequestId(supportRequestId)) {
			throw new ForbiddenOperationException(
					"SupportRequest " + supportRequestId + " already has a mentoring call proposal");
		}
	}

	private void validateTeamRegisteredForHackathon(Long hackathonId, Long teamId) {
		boolean teamRegisteredForHackathon =
				hackathonRegistrationRepository.existsByHackathonIdAndTeamId(hackathonId, teamId);

		if (!teamRegisteredForHackathon) {
			throw new ForbiddenOperationException(
					"Team " + teamId + " is not registered for hackathon " + hackathonId);
		}
	}

	private void validateBookingLink(String bookingLink) {
		if (bookingLink == null || bookingLink.isBlank()) {
			throw new IllegalStateException("Calendar gateway returned an empty booking link");
		}
	}

	private String normalizeRequiredDescription(String description) {
		if (description == null || description.isBlank()) {
			throw new IllegalArgumentException("Violation report description must not be blank");
		}

		return description.strip();
	}

}
