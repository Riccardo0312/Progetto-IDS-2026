package it.unicam.cs.ids.hackhub.services;

import it.unicam.cs.ids.hackhub.model.*;
import it.unicam.cs.ids.hackhub.model.repository.InvitationRepository;
import it.unicam.cs.ids.hackhub.model.repository.TeamMemberRepository;
import it.unicam.cs.ids.hackhub.model.repository.TeamRepository;
import it.unicam.cs.ids.hackhub.model.repository.UserRepository;
import jakarta.transaction.Transactional;


public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;

    public InvitationService(InvitationRepository invitationRepository, UserRepository userRepository, TeamMemberRepository teamMemberRepository, TeamRepository teamRepository) {
        this.invitationRepository = invitationRepository;
        this.userRepository = userRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.teamRepository = teamRepository;
    }

    @Transactional
    public Invitation sendInvitation(Long teamId, String recipientEmail, String senderEmail) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team non trovato"));
        User recipient = UserRepository.findByEmail(recipientEmail)
                .orElseThrow(() -> new IllegalArgumentException("Destinatario non trovato"));

        Invitation invitation = new Invitation();
        invitation.setTeam(team);
        invitation.setRecipient(recipient);
        invitation.setStatus(InvitationStatus.PENDING);

        return invitationRepository.save(invitation);
    }

    @Transactional
    public Invitation acceptInvitation(Long invitationId, String userEmail) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invito non trovato"));
        User user = UserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));

        if (!invitation.getRecipient().equals(user)) {
            throw new IllegalArgumentException("L'invito non è per questo utente");
        }

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepository.save(invitation);

        TeamMember member = new TeamMember();
        member.setTeam(invitation.getTeam());
        member.setUser(user);
        teamMemberRepository.save(member);

        return invitation;
    }
}