package it.unicam.cs.ids.hackhub.service.impl;

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
import org.springframework.stereotype.Service;

@Service
public class MentoringService implements IMentoringRequestService {

    private final SupportRequestRepository supportRequestRepository;
    private final HackathonRegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;

    public MentoringService(SupportRequestRepository supportRequestRepository,
                            HackathonRegistrationRepository registrationRepository,
                            UserRepository userRepository,
                            TeamMemberRepository teamMemberRepository) {
        this.supportRequestRepository = supportRequestRepository;
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    @Override
    @Transactional
    public SupportRequest createSupportRequest(Long registrationId, String userEmail, String description) {
        String normalizedDescription = normalizeRequiredDescription(description);
        HackathonRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registrazione", registrationId));

        ensureCallerIsTeamMember(registration.getTeam(), userEmail);
        ensureHackathonAcceptsSupportRequests(registration.getHackathon());

        SupportRequest request = new SupportRequest();
        request.setTeam(registration.getTeam());
        request.setHackathon(registration.getHackathon());
        request.setMessage(normalizedDescription);
        request.setRequestedAt(LocalDateTime.now());

        return supportRequestRepository.save(request);
    }

    private void ensureCallerIsTeamMember(Team team, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", userEmail));
        if (!teamMemberRepository.existsByTeamIdAndUserId(team.getId(), user.getId())) {
            throw new ForbiddenOperationException(
                    "Solo i membri del team possono richiedere supporto");
        }
    }

    private void ensureHackathonAcceptsSupportRequests(Hackathon hackathon) {
        hackathon.updateStatus();
        hackathon.ensureSupportRequestsAllowed();
    }

    private String normalizeRequiredDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Support request description must not be blank");
        }

        return description.strip();
    }
}
