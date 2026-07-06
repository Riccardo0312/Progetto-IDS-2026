package it.unicam.cs.ids.hackhub.service.impl;

import it.unicam.cs.ids.hackhub.config.CurrentDateProvider;
import it.unicam.cs.ids.hackhub.exception.ForbiddenOperationException;
import it.unicam.cs.ids.hackhub.exception.ResourceNotFoundException;
import it.unicam.cs.ids.hackhub.model.Hackathon;
import it.unicam.cs.ids.hackhub.model.HackathonRegistration;
import it.unicam.cs.ids.hackhub.model.SupportRequest;
import it.unicam.cs.ids.hackhub.model.Team;
import it.unicam.cs.ids.hackhub.model.User;
import it.unicam.cs.ids.hackhub.model.repository.HackathonRegistrationRepository;
import it.unicam.cs.ids.hackhub.model.repository.SupportRequestRepository;
import it.unicam.cs.ids.hackhub.model.repository.TeamMemberRepository;
import it.unicam.cs.ids.hackhub.model.repository.UserRepository;
import it.unicam.cs.ids.hackhub.service.interfaces.IMentoringRequestService;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MentoringService implements IMentoringRequestService {

    private final SupportRequestRepository supportRequestRepository;
    private final HackathonRegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final CurrentDateProvider currentDateProvider;

    public MentoringService(SupportRequestRepository supportRequestRepository,
                            HackathonRegistrationRepository registrationRepository,
                            UserRepository userRepository,
                            TeamMemberRepository teamMemberRepository,
                            CurrentDateProvider currentDateProvider) {
        this.supportRequestRepository = supportRequestRepository;
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.currentDateProvider = currentDateProvider;
    }

	@Override
	public List<SupportRequest> getTeamSupportRequests(Long teamId, String userEmail) {
		ensureCallerIsTeamMember(teamId, userEmail);
		return supportRequestRepository.findByTeamId(teamId);
	}

	@Override
	public SupportRequest getTeamSupportRequest(Long teamId, Long supportRequestId, String userEmail) {
		ensureCallerIsTeamMember(teamId, userEmail);
		return supportRequestRepository.findByIdAndTeamId(supportRequestId, teamId)
				.orElseThrow(() -> new ResourceNotFoundException("SupportRequest", supportRequestId));
	}

	@Override
	@Transactional
	public SupportRequest createSupportRequest(Long registrationId, String userEmail, String description) {
        String normalizedDescription = normalizeRequiredDescription(description);
        HackathonRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registrazione", registrationId));

        ensureCallerIsTeamMember(registration.getTeam(), userEmail);
        ensureHackathonAcceptsSupportRequests(registration.getHackathon());
        ensureTeamNotDisqualified(registration);

        SupportRequest request = new SupportRequest();
        request.setTeam(registration.getTeam());
        request.setHackathon(registration.getHackathon());
        request.setMessage(normalizedDescription);
        request.setRequestedAt(LocalDateTime.now());

        return supportRequestRepository.save(request);
    }

	private void ensureCallerIsTeamMember(Team team, String userEmail) {
		ensureCallerIsTeamMember(
				team.getId(), userEmail, "Solo i membri del team possono richiedere supporto");
	}

	private void ensureCallerIsTeamMember(Long teamId, String userEmail) {
		ensureCallerIsTeamMember(
				teamId, userEmail, "Solo i membri del team possono consultare le richieste di supporto");
	}

	private void ensureCallerIsTeamMember(Long teamId, String userEmail, String forbiddenMessage) {
		User user = userRepository.findByEmail(userEmail)
				.orElseThrow(() -> new ResourceNotFoundException("Utente", userEmail));
		if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, user.getId())) {
			throw new ForbiddenOperationException(forbiddenMessage);
		}
	}

    private void ensureHackathonAcceptsSupportRequests(Hackathon hackathon) {
        hackathon.updateStatus(currentDateProvider.today());
        hackathon.ensureSupportRequestsAllowed();
    }

    private void ensureTeamNotDisqualified(HackathonRegistration registration) {
        if (registration.isDisqualified()) {
            throw new ForbiddenOperationException(
                    "Il team è squalificato dall'hackathon e non può inviare richieste di supporto");
        }
    }

    private String normalizeRequiredDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Support request description must not be blank");
        }

        return description.strip();
    }
}
