package it.unicam.cs.ids.hackhub.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import it.unicam.cs.ids.hackhub.exception.ForbiddenOperationException;
import it.unicam.cs.ids.hackhub.model.Invitation;
import it.unicam.cs.ids.hackhub.model.InvitationStatus;
import it.unicam.cs.ids.hackhub.model.Team;
import it.unicam.cs.ids.hackhub.model.TeamMember;
import it.unicam.cs.ids.hackhub.model.TeamRole;
import it.unicam.cs.ids.hackhub.model.User;
import it.unicam.cs.ids.hackhub.model.repository.InvitationRepository;
import it.unicam.cs.ids.hackhub.model.repository.TeamMemberRepository;
import it.unicam.cs.ids.hackhub.model.repository.TeamRepository;
import it.unicam.cs.ids.hackhub.model.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

    @Mock InvitationRepository invitationRepository;
    @Mock UserRepository userRepository;
    @Mock TeamMemberRepository teamMemberRepository;
    @Mock TeamRepository teamRepository;

    @InjectMocks InvitationService invitationService;

    private User leader;
    private User member;
    private User outsider;
    private Team team;

    @BeforeEach
    void setUp() {
        leader = buildUser(1L, "leader@test.it");
        member = buildUser(2L, "member@test.it");
        outsider = buildUser(3L, "outsider@test.it");

        team = new Team();
        team.setId(10L);
        team.setName("TestTeam");
    }

    @Test
    void sendInvitation_succeedsWhenSenderIsLeader() {
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        when(userRepository.findByEmail("outsider@test.it")).thenReturn(Optional.of(outsider));
        when(userRepository.findByEmail("leader@test.it")).thenReturn(Optional.of(leader));
        when(teamMemberRepository.existsByTeamIdAndUserIdAndRole(10L, 1L, TeamRole.LEADER)).thenReturn(true);
        when(teamMemberRepository.existsByUserId(3L)).thenReturn(false);
        when(invitationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Invitation result = invitationService.sendInvitation(10L, "outsider@test.it", "leader@test.it");

        assertThat(result.getStatus()).isEqualTo(InvitationStatus.PENDING);
        assertThat(result.getRecipient()).isEqualTo(outsider);
    }

    @Test
    void sendInvitation_throwsWhenSenderIsNotLeader() {
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        when(userRepository.findByEmail("outsider@test.it")).thenReturn(Optional.of(outsider));
        when(userRepository.findByEmail("member@test.it")).thenReturn(Optional.of(member));
        when(teamMemberRepository.existsByTeamIdAndUserIdAndRole(10L, 2L, TeamRole.LEADER)).thenReturn(false);

        assertThatThrownBy(() ->
                invitationService.sendInvitation(10L, "outsider@test.it", "member@test.it"))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessageContaining("Solo il leader");
    }

    @Test
    void acceptInvitation_assignsAcceptedUserAsMember() {
        Invitation invitation = new Invitation();
        invitation.setId(50L);
        invitation.setTeam(team);
        invitation.setRecipient(outsider);
        invitation.setStatus(InvitationStatus.PENDING);

        when(invitationRepository.findById(50L)).thenReturn(Optional.of(invitation));
        when(userRepository.findByEmail("outsider@test.it")).thenReturn(Optional.of(outsider));
        when(teamMemberRepository.existsByUserId(3L)).thenReturn(false);
        when(invitationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(teamMemberRepository.save(any())).thenAnswer(inv -> {
            TeamMember saved = inv.getArgument(0);
            assertThat(saved.getRole()).isEqualTo(TeamRole.MEMBER);
            return saved;
        });

        invitationService.acceptInvitation(50L, "outsider@test.it");

        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
    }

    private User buildUser(Long id, String email) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setName("User" + id);
        u.setPassword("password1");
        return u;
    }

    // --- getPendingInvitations ---

    @Test
    void getPendingInvitations_returnsOnlyPendingForRecipient() {
        Invitation inv1 = buildInvitation(1L, outsider, team);
        Invitation inv2 = buildInvitation(2L, outsider, team);

        when(userRepository.findByEmail("outsider@test.it")).thenReturn(Optional.of(outsider));
        when(teamMemberRepository.existsByUserId(3L)).thenReturn(false);
        when(invitationRepository.findByRecipientIdAndStatusOrderByIdDesc(3L, InvitationStatus.PENDING))
                .thenReturn(List.of(inv2, inv1));

        List<Invitation> result = invitationService.getPendingInvitations("outsider@test.it");

        assertThat(result).hasSize(2).containsExactly(inv2, inv1);
    }

    @Test
    void getPendingInvitations_returnsEmptyWhenUserAlreadyInTeam() {
        when(userRepository.findByEmail("outsider@test.it")).thenReturn(Optional.of(outsider));
        when(teamMemberRepository.existsByUserId(3L)).thenReturn(true);

        List<Invitation> result = invitationService.getPendingInvitations("outsider@test.it");

        assertThat(result).isEmpty();
    }

    @Test
    void getPendingInvitations_returnsEmptyWhenNoInvitations() {
        when(userRepository.findByEmail("outsider@test.it")).thenReturn(Optional.of(outsider));
        when(teamMemberRepository.existsByUserId(3L)).thenReturn(false);
        when(invitationRepository.findByRecipientIdAndStatusOrderByIdDesc(3L, InvitationStatus.PENDING))
                .thenReturn(List.of());

        List<Invitation> result = invitationService.getPendingInvitations("outsider@test.it");

        assertThat(result).isEmpty();
    }

    private Invitation buildInvitation(Long id, User recipient, Team team) {
        Invitation inv = new Invitation();
        inv.setId(id);
        inv.setRecipient(recipient);
        inv.setTeam(team);
        inv.setStatus(InvitationStatus.PENDING);
        return inv;
    }
}
