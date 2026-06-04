package it.unicam.cs.ids.hackhub.service.impl;

import it.unicam.cs.ids.hackhub.exception.ForbiddenOperationException;
import it.unicam.cs.ids.hackhub.exception.ResourceNotFoundException;
import it.unicam.cs.ids.hackhub.model.*;
import it.unicam.cs.ids.hackhub.model.repository.InvitationRepository;
import it.unicam.cs.ids.hackhub.model.repository.TeamMemberRepository;
import it.unicam.cs.ids.hackhub.model.repository.TeamRepository;
import it.unicam.cs.ids.hackhub.model.repository.UserRepository;
import it.unicam.cs.ids.hackhub.service.interfaces.IInvitationService;
import jakarta.transaction.Transactional;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class InvitationService implements IInvitationService {

    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;

    public InvitationService(InvitationRepository invitationRepository,
                             UserRepository userRepository,
                             TeamMemberRepository teamMemberRepository,
                             TeamRepository teamRepository) {
        this.invitationRepository = invitationRepository;
        this.userRepository = userRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.teamRepository = teamRepository;
    }

    @Override
    @Transactional
    public Invitation sendInvitation(Long teamId, String recipientEmail, String senderEmail) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", teamId));
        User recipient = userRepository.findByEmail(recipientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", recipientEmail));
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", senderEmail));

        validateSenderIsLeader(team, sender);
        validateUserCanJoinTeam(recipient);

        Invitation invitation = new Invitation();
        invitation.setTeam(team);
        invitation.setRecipient(recipient);
        invitation.setStatus(InvitationStatus.PENDING);

        return invitationRepository.save(invitation);
    }

    @Override
    @Transactional
    public Invitation acceptInvitation(Long invitationId, String userEmail) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invito", invitationId));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", userEmail));

        if (!isInvitationRecipient(invitation, user)) {
            throw new IllegalArgumentException("L'invito non è per questo utente");
        }

        validateUserCanJoinTeam(user);
        invitation.accept();
        invitationRepository.save(invitation);

        // Il nuovo membro entra sempre come MEMBER (mai come LEADER).
        TeamMember member = new TeamMember(user, invitation.getTeam(), TeamRole.MEMBER);
        TeamMember savedMember = teamMemberRepository.save(member);
        invitation.getTeam().getMembers().add(savedMember);

        return invitation;
    }

    @Override
    @Transactional
    public Invitation rejectInvitation(Long invitationId, String userEmail) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("Invito", invitationId));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", userEmail));

        if (!isInvitationRecipient(invitation, user)) {
            throw new IllegalArgumentException("L'invito non è per questo utente");
        }

        invitation.reject();
        return invitationRepository.save(invitation);
    }

    /** Solo il leader del team può inviare inviti. */
    private void validateSenderIsLeader(Team team, User sender) {
        boolean senderIsLeader = teamMemberRepository
                .existsByTeamIdAndUserIdAndRole(team.getId(), sender.getId(), TeamRole.LEADER);
        if (!senderIsLeader) {
            throw new ForbiddenOperationException("Solo il leader del team può inviare inviti");
        }
    }

    private void validateUserCanJoinTeam(User user) {
        if (teamMemberRepository.existsByUserId(user.getId())) {
            throw new IllegalArgumentException("L'utente appartiene già a un team");
        }
    }

    private boolean isInvitationRecipient(Invitation invitation, User user) {
        return invitation.getRecipient() != null
                && Objects.equals(invitation.getRecipient().getId(), user.getId());
    }
}
