package it.unicam.cs.ids.hackhub.service.impl;

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

    public InvitationService(InvitationRepository invitationRepository, UserRepository userRepository,
                             TeamMemberRepository teamMemberRepository, TeamRepository teamRepository) {
        this.invitationRepository = invitationRepository;
        this.userRepository = userRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.teamRepository = teamRepository;
    }

    @Override
    @Transactional
    public Invitation sendInvitation(Long teamId, String recipientEmail, String senderEmail) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team non trovato"));
        User recipient = userRepository.findByEmail(recipientEmail)
                .orElseThrow(() -> new IllegalArgumentException("Destinatario non trovato"));
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new IllegalArgumentException("Mittente non trovato"));

        validateSenderCanInvite(team, sender);
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
                .orElseThrow(() -> new IllegalArgumentException("Invito non trovato"));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));

        if (!isInvitationRecipient(invitation, user)) {
            throw new IllegalArgumentException("L'invito non è per questo utente");
        }

        validateUserCanJoinTeam(user);
        invitation.accept();
        invitationRepository.save(invitation);

        TeamMember member = new TeamMember();
        member.setTeam(invitation.getTeam());
        member.setUser(user);
        TeamMember savedMember = teamMemberRepository.save(member);
        invitation.getTeam().getMembers().add(savedMember);

        return invitation;
    }

    @Override
    @Transactional
    public Invitation rejectInvitation(Long invitationId, String userEmail) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invito non trovato"));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));

        if (!isInvitationRecipient(invitation, user)) {
            throw new IllegalArgumentException("L'invito non è per questo utente");
        }

        invitation.reject();
        return invitationRepository.save(invitation);
    }

    private void validateSenderCanInvite(Team team, User sender) {
        boolean senderIsCreator =
                team.getCreator() != null && Objects.equals(team.getCreator().getId(), sender.getId());
        boolean senderIsTeamMember =
                teamMemberRepository.existsByTeamIdAndUserId(team.getId(), sender.getId());

        if (!senderIsCreator && !senderIsTeamMember) {
            throw new IllegalArgumentException("Il mittente non appartiene al team");
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
