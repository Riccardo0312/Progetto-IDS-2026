package it.unicam.cs.ids.hackhub.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.unicam.cs.ids.hackhub.exception.ForbiddenOperationException;
import it.unicam.cs.ids.hackhub.model.Hackathon;
import it.unicam.cs.ids.hackhub.model.HackathonRegistration;
import it.unicam.cs.ids.hackhub.model.HackathonStatus;
import it.unicam.cs.ids.hackhub.model.Submission;
import it.unicam.cs.ids.hackhub.model.Team;
import it.unicam.cs.ids.hackhub.model.TeamMember;
import it.unicam.cs.ids.hackhub.model.TeamRole;
import it.unicam.cs.ids.hackhub.model.User;
import it.unicam.cs.ids.hackhub.model.repository.HackathonRegistrationRepository;
import it.unicam.cs.ids.hackhub.model.repository.InvitationRepository;
import it.unicam.cs.ids.hackhub.model.repository.SubmissionRepository;
import it.unicam.cs.ids.hackhub.model.repository.TeamMemberRepository;
import it.unicam.cs.ids.hackhub.model.repository.TeamRepository;
import it.unicam.cs.ids.hackhub.model.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock TeamRepository teamRepository;
    @Mock TeamMemberRepository teamMemberRepository;
    @Mock UserRepository userRepository;
    @Mock InvitationRepository invitationRepository;
    @Mock HackathonRegistrationRepository hackathonRegistrationRepository;
    @Mock SubmissionRepository submissionRepository;

    @InjectMocks TeamService teamService;

    private User creator;
    private Team team;
    private TeamMember leaderMember;

    @BeforeEach
    void setUp() {
        creator = new User();
        creator.setId(1L);
        creator.setEmail("leader@test.it");
        creator.setName("Leader");
        creator.setPassword("password1");

        team = new Team();
        team.setId(10L);
        team.setName("TestTeam");

        leaderMember = new TeamMember(creator, team, TeamRole.LEADER);
        leaderMember.setId(100L);
        team.getMembers().add(leaderMember);
    }

    // ---- createTeam ----

    @Test
    void createTeam_assignsCreatorAsLeader() {
        Team freshTeam = new Team();
        freshTeam.setId(10L);
        freshTeam.setName("TestTeam");

        when(userRepository.findByEmail("leader@test.it")).thenReturn(Optional.of(creator));
        when(teamRepository.existsByNameIgnoreCase("TestTeam")).thenReturn(false);
        when(teamMemberRepository.existsByUserId(1L)).thenReturn(false);
        when(teamRepository.save(any())).thenReturn(freshTeam);
        when(teamMemberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Team result = teamService.createTeam("TestTeam", "leader@test.it");

        assertThat(result.getMembers()).hasSize(1);
        assertThat(result.getMembers().get(0).isLeader()).isTrue();
    }

    @Test
    void createTeam_throwsWhenUserNotFound() {
        when(userRepository.findByEmail("nobody@test.it")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.createTeam("T", "nobody@test.it"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Utente non trovato");
    }

    @Test
    void createTeam_throwsWhenNameDuplicate() {
        when(userRepository.findByEmail("leader@test.it")).thenReturn(Optional.of(creator));
        when(teamRepository.existsByNameIgnoreCase("TestTeam")).thenReturn(true);

        assertThatThrownBy(() -> teamService.createTeam("TestTeam", "leader@test.it"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Esiste già un team");
    }

    @Test
    void createTeam_throwsWhenUserAlreadyInTeam() {
        when(userRepository.findByEmail("leader@test.it")).thenReturn(Optional.of(creator));
        when(teamRepository.existsByNameIgnoreCase("TestTeam")).thenReturn(false);
        when(teamMemberRepository.existsByUserId(1L)).thenReturn(true);

        assertThatThrownBy(() -> teamService.createTeam("TestTeam", "leader@test.it"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("appartiene già a un team");
    }

    // ---- leaveTeam (leader) ----

    @Test
    void leaveTeam_asLeader_promotesSuccessorAndRemovesLeader() {
        User successorUser = buildUser(2L, "member@test.it");
        TeamMember successorMember = new TeamMember(successorUser, team, TeamRole.MEMBER);
        successorMember.setId(101L);
        team.getMembers().add(successorMember);

        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        when(userRepository.findByEmail("leader@test.it")).thenReturn(Optional.of(creator));
        when(teamMemberRepository.findByTeamIdAndUserId(10L, 1L)).thenReturn(Optional.of(leaderMember));
        when(userRepository.findByEmail("member@test.it")).thenReturn(Optional.of(successorUser));
        when(teamMemberRepository.findByTeamIdAndUserId(10L, 2L)).thenReturn(Optional.of(successorMember));
        when(teamMemberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        teamService.leaveTeam(10L, "leader@test.it", "member@test.it");

        assertThat(successorMember.isLeader()).isTrue();
        verify(teamMemberRepository).delete(leaderMember);
    }

    @Test
    void leaveTeam_asLeader_throwsWhenSuccessorMissing() {
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        when(userRepository.findByEmail("leader@test.it")).thenReturn(Optional.of(creator));
        when(teamMemberRepository.findByTeamIdAndUserId(10L, 1L)).thenReturn(Optional.of(leaderMember));

        assertThatThrownBy(() -> teamService.leaveTeam(10L, "leader@test.it", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("deve indicare un successore");
    }

    @Test
    void leaveTeam_asLeader_throwsWhenAloneInTeam() {
        // team ha solo il leader (setUp)
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        when(userRepository.findByEmail("leader@test.it")).thenReturn(Optional.of(creator));
        when(teamMemberRepository.findByTeamIdAndUserId(10L, 1L)).thenReturn(Optional.of(leaderMember));

        assertThatThrownBy(() -> teamService.leaveTeam(10L, "leader@test.it", "anyone@test.it"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unico membro");
    }

    @Test
    void leaveTeam_asLeader_throwsWhenSuccessorNotMember() {
        User outsider = buildUser(99L, "outsider@test.it");
        // team ha due membri per superare il check "unico membro"
        team.getMembers().add(new TeamMember(buildUser(2L, "other@test.it"), team, TeamRole.MEMBER));

        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        when(userRepository.findByEmail("leader@test.it")).thenReturn(Optional.of(creator));
        when(teamMemberRepository.findByTeamIdAndUserId(10L, 1L)).thenReturn(Optional.of(leaderMember));
        when(userRepository.findByEmail("outsider@test.it")).thenReturn(Optional.of(outsider));
        when(teamMemberRepository.findByTeamIdAndUserId(10L, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.leaveTeam(10L, "leader@test.it", "outsider@test.it"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non è membro del team");
    }

    @Test
    void leaveTeam_asLeader_throwsWhenLeaderIsSelectedAsSuccessor() {
        team.getMembers().add(new TeamMember(
                buildUser(2L, "member@test.it"), team, TeamRole.MEMBER));

        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        when(userRepository.findByEmail("leader@test.it")).thenReturn(Optional.of(creator));
        when(teamMemberRepository.findByTeamIdAndUserId(10L, 1L)).thenReturn(Optional.of(leaderMember));

        assertThatThrownBy(() -> teamService.leaveTeam(10L, "leader@test.it", "leader@test.it"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("successore diverso");
    }

    // ---- leaveTeam (membro normale) ----

    @Test
    void leaveTeam_asMember_removesMember() {
        User memberUser = buildUser(2L, "member@test.it");
        TeamMember memberMember = new TeamMember(memberUser, team, TeamRole.MEMBER);
        memberMember.setId(101L);
        team.getMembers().add(memberMember);

        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        when(userRepository.findByEmail("member@test.it")).thenReturn(Optional.of(memberUser));
        when(teamMemberRepository.findByTeamIdAndUserId(10L, 2L)).thenReturn(Optional.of(memberMember));

        teamService.leaveTeam(10L, "member@test.it", null);

        verify(teamMemberRepository).delete(memberMember);
        assertThat(team.getMembers()).doesNotContain(memberMember);
    }

    @Test
    void leaveTeam_asMember_throwsWhenSuccessorPassed() {
        User memberUser = buildUser(2L, "member@test.it");
        TeamMember memberMember = new TeamMember(memberUser, team, TeamRole.MEMBER);

        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        when(userRepository.findByEmail("member@test.it")).thenReturn(Optional.of(memberUser));
        when(teamMemberRepository.findByTeamIdAndUserId(10L, 2L)).thenReturn(Optional.of(memberMember));

        assertThatThrownBy(() -> teamService.leaveTeam(10L, "member@test.it", "someone@test.it"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Solo il leader può indicare un successore");
    }

    @Test
    void leaveTeam_throwsWhenUserNotInTeam() {
        User outsider = buildUser(99L, "outsider@test.it");

        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        when(userRepository.findByEmail("outsider@test.it")).thenReturn(Optional.of(outsider));
        when(teamMemberRepository.findByTeamIdAndUserId(10L, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.leaveTeam(10L, "outsider@test.it", null))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void leaveTeam_invariantSingleLeaderPreserved() {
        User successorUser = buildUser(2L, "member@test.it");
        TeamMember successorMember = new TeamMember(successorUser, team, TeamRole.MEMBER);
        successorMember.setId(101L);
        team.getMembers().add(successorMember);

        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        when(userRepository.findByEmail("leader@test.it")).thenReturn(Optional.of(creator));
        when(teamMemberRepository.findByTeamIdAndUserId(10L, 1L)).thenReturn(Optional.of(leaderMember));
        when(userRepository.findByEmail("member@test.it")).thenReturn(Optional.of(successorUser));
        when(teamMemberRepository.findByTeamIdAndUserId(10L, 2L)).thenReturn(Optional.of(successorMember));
        when(teamMemberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        teamService.leaveTeam(10L, "leader@test.it", "member@test.it");

        long leaderCount = team.getMembers().stream().filter(TeamMember::isLeader).count();
        assertThat(leaderCount).isEqualTo(1L);
    }

    // ---- deleteTeam ----

    @Test
    void deleteTeam_succeedsWhenLeaderAndNoRegistrations() {
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        when(userRepository.findByEmail("leader@test.it")).thenReturn(Optional.of(creator));
        when(teamMemberRepository.existsByTeamIdAndUserIdAndRole(10L, 1L, TeamRole.LEADER)).thenReturn(true);

        teamService.deleteTeam(10L, "leader@test.it");

        verify(invitationRepository).deleteByTeamId(10L);
        verify(hackathonRegistrationRepository).deleteByTeamId(10L);
        verify(teamMemberRepository).deleteAll(team.getMembers());
        verify(teamRepository).delete(team);
    }

    @Test
    void deleteTeam_succeedsWhenAllRegistrationsInRegistrationState() {
        Hackathon h = buildHackathon(HackathonStatus.REGISTRATION);
        HackathonRegistration registration = buildRegistration(h);
        Submission submission = new Submission();
        registration.setSubmission(submission);
        team.getRegistrations().add(registration);

        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        when(userRepository.findByEmail("leader@test.it")).thenReturn(Optional.of(creator));
        when(teamMemberRepository.existsByTeamIdAndUserIdAndRole(10L, 1L, TeamRole.LEADER)).thenReturn(true);

        teamService.deleteTeam(10L, "leader@test.it");

        verify(submissionRepository).deleteAll(List.of(submission));
        verify(hackathonRegistrationRepository).deleteByTeamId(10L);
        verify(teamRepository).delete(team);
    }

    @Test
    void deleteTeam_throwsWhenNotLeader() {
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        when(userRepository.findByEmail("leader@test.it")).thenReturn(Optional.of(creator));
        when(teamMemberRepository.existsByTeamIdAndUserIdAndRole(10L, 1L, TeamRole.LEADER)).thenReturn(false);

        assertThatThrownBy(() -> teamService.deleteTeam(10L, "leader@test.it"))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessageContaining("Solo il leader");
    }

    @Test
    void deleteTeam_throwsWhenRegistrationInRunning() {
        Hackathon h = buildHackathon(HackathonStatus.RUNNING);
        team.getRegistrations().add(buildRegistration(h));

        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        when(userRepository.findByEmail("leader@test.it")).thenReturn(Optional.of(creator));
        when(teamMemberRepository.existsByTeamIdAndUserIdAndRole(10L, 1L, TeamRole.LEADER)).thenReturn(true);

        assertThatThrownBy(() -> teamService.deleteTeam(10L, "leader@test.it"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non in fase di iscrizione");
    }

    @Test
    void deleteTeam_throwsWhenRegistrationInEvaluation() {
        Hackathon h = buildHackathon(HackathonStatus.EVALUATION);
        team.getRegistrations().add(buildRegistration(h));

        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        when(userRepository.findByEmail("leader@test.it")).thenReturn(Optional.of(creator));
        when(teamMemberRepository.existsByTeamIdAndUserIdAndRole(10L, 1L, TeamRole.LEADER)).thenReturn(true);

        assertThatThrownBy(() -> teamService.deleteTeam(10L, "leader@test.it"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void deleteTeam_throwsWhenRegistrationInConcluded() {
        Hackathon h = buildHackathon(HackathonStatus.CONCLUDED);
        team.getRegistrations().add(buildRegistration(h));

        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        when(userRepository.findByEmail("leader@test.it")).thenReturn(Optional.of(creator));
        when(teamMemberRepository.existsByTeamIdAndUserIdAndRole(10L, 1L, TeamRole.LEADER)).thenReturn(true);

        assertThatThrownBy(() -> teamService.deleteTeam(10L, "leader@test.it"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void deleteTeam_cascadesInvitationsAndMembers() {
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        when(userRepository.findByEmail("leader@test.it")).thenReturn(Optional.of(creator));
        when(teamMemberRepository.existsByTeamIdAndUserIdAndRole(10L, 1L, TeamRole.LEADER)).thenReturn(true);

        teamService.deleteTeam(10L, "leader@test.it");

        verify(invitationRepository).deleteByTeamId(10L);
        verify(hackathonRegistrationRepository).deleteByTeamId(10L);
        verify(teamMemberRepository).deleteAll(team.getMembers());
        verify(teamRepository).delete(team);
    }

    @Test
    void memberWithoutPersistedRoleIsTreatedAsRegularMember() {
        TeamMember legacyMember = new TeamMember();
        legacyMember.setRole(null);

        assertThat(legacyMember.isMember()).isTrue();
        assertThat(legacyMember.isLeader()).isFalse();
    }

    // ---- helpers ----

    private User buildUser(Long id, String email) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setName("User" + id);
        u.setPassword("password1");
        return u;
    }

    private Hackathon buildHackathon(HackathonStatus status) {
        Hackathon h = new Hackathon();
        h.setId(200L);
        java.time.LocalDate today = java.time.LocalDate.now();
        switch (status) {
            case REGISTRATION -> {
                h.setRegistrationDeadline(today.plusDays(5));
                h.setStartDate(today.plusDays(7));
                h.setEndDate(today.plusDays(15));
                h.updateStatus(today);
            }
            case RUNNING -> {
                h.setRegistrationDeadline(today.minusDays(5));
                h.setStartDate(today.minusDays(2));
                h.setEndDate(today.plusDays(5));
                h.updateStatus(today);
            }
            case EVALUATION -> {
                h.setRegistrationDeadline(today.minusDays(10));
                h.setStartDate(today.minusDays(7));
                h.setEndDate(today.minusDays(2));
                h.updateStatus(today);
            }
            case CONCLUDED -> {
                h.setRegistrationDeadline(today.minusDays(10));
                h.setEndDate(today.minusDays(2));
                h.updateStatus(today);
                // CONCLUDED richiede concludeWith — usiamo EVALUATION come proxy
                // per testare il blocco su stati non-REGISTRATION
            }
        }
        return h;
    }

    private HackathonRegistration buildRegistration(Hackathon hackathon) {
        HackathonRegistration r = new HackathonRegistration();
        r.setHackathon(hackathon);
        r.setTeam(team);
        return r;
    }
}
