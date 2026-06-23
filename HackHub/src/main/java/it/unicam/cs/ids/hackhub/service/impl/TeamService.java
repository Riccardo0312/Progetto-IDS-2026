package it.unicam.cs.ids.hackhub.service.impl;

import it.unicam.cs.ids.hackhub.dto.team.HackathonSummaryDTO;
import it.unicam.cs.ids.hackhub.dto.team.TeamDetailsDTO;
import it.unicam.cs.ids.hackhub.dto.team.UserSummaryDTO;
import it.unicam.cs.ids.hackhub.exception.ForbiddenOperationException;
import it.unicam.cs.ids.hackhub.exception.ResourceNotFoundException;
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
import it.unicam.cs.ids.hackhub.service.interfaces.ITeamService;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class TeamService implements ITeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;
    private final HackathonRegistrationRepository hackathonRegistrationRepository;
    private final SubmissionRepository submissionRepository;

    public TeamService(TeamRepository teamRepository,
                       TeamMemberRepository teamMemberRepository,
                       UserRepository userRepository,
                       InvitationRepository invitationRepository,
                       HackathonRegistrationRepository hackathonRegistrationRepository,
                       SubmissionRepository submissionRepository) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.userRepository = userRepository;
        this.invitationRepository = invitationRepository;
        this.hackathonRegistrationRepository = hackathonRegistrationRepository;
        this.submissionRepository = submissionRepository;
    }

    @Override
    @Transactional
    public Team createTeam(String name, String creatorEmail) {
        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", creatorEmail));

        if (teamRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Esiste già un team con questo nome");
        }
        if (teamMemberRepository.existsByUserId(creator.getId())) {
            throw new IllegalArgumentException("L'utente appartiene già a un team");
        }

        Team team = new Team();
        team.setName(name);
        Team savedTeam = teamRepository.save(team);

        TeamMember leader = new TeamMember(creator, savedTeam, TeamRole.LEADER);
        TeamMember savedLeader = teamMemberRepository.save(leader);
        savedTeam.getMembers().add(savedLeader);

        return savedTeam;
    }

    @Override
    @Transactional
    public void leaveTeam(Long teamId, String userEmail, String successorEmail) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", teamId));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", userEmail));

        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, user.getId())
                .orElseThrow(() -> new ForbiddenOperationException(
                        "L'utente non appartiene al team"));

        if (member.isLeader()) {
            leaveAsLeader(team, member, successorEmail);
        } else {
            leaveAsMember(member, successorEmail);
        }
    }

    @Override
    @Transactional
    public void deleteTeam(Long teamId, String leaderEmail) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", teamId));
        User user = userRepository.findByEmail(leaderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", leaderEmail));

        if (!teamMemberRepository.existsByTeamIdAndUserIdAndRole(
                teamId, user.getId(), TeamRole.LEADER)) {
            throw new ForbiddenOperationException("Solo il leader può eliminare il team");
        }

        boolean hasBlockingRegistration = team.getRegistrations().stream()
                .anyMatch(r -> {
                    r.getHackathon().updateStatus();
                    return r.getHackathon().getStatus() != HackathonStatus.REGISTRATION;
                });
        if (hasBlockingRegistration) {
            throw new IllegalStateException(
                    "Il team è iscritto a uno o più hackathon non in fase di iscrizione. "
                            + "Impossibile eliminarlo.");
        }

        invitationRepository.deleteByTeamId(teamId);
        List<Submission> submissions = team.getRegistrations().stream()
                .map(registration -> registration.getSubmission())
                .filter(Objects::nonNull)
                .toList();
        submissionRepository.deleteAll(submissions);
        hackathonRegistrationRepository.deleteByTeamId(teamId);
        teamMemberRepository.deleteAll(team.getMembers());
        teamRepository.delete(team);
    }

    private void leaveAsLeader(Team team, TeamMember leader, String successorEmail) {
        if (successorEmail == null || successorEmail.isBlank()) {
            throw new IllegalArgumentException(
                    "Il leader deve indicare un successore per lasciare il team");
        }
        if (team.getMembers().size() <= 1) {
            throw new IllegalStateException(
                    "Il leader è l'unico membro del team. Usa deleteTeam per scioglierlo");
        }

        User successorUser = userRepository.findByEmail(successorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", successorEmail));
        if (Objects.equals(leader.getUser().getId(), successorUser.getId())) {
            throw new IllegalArgumentException(
                    "Il leader deve indicare un successore diverso da se stesso");
        }

        TeamMember successor = teamMemberRepository
                .findByTeamIdAndUserId(team.getId(), successorUser.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Il successore non è membro del team"));

        team.promoteToLeader(successor);
        teamMemberRepository.save(successor);
        teamMemberRepository.delete(leader);
        team.getMembers().remove(leader);
    }

    private void leaveAsMember(TeamMember member, String successorEmail) {
        if (successorEmail != null && !successorEmail.isBlank()) {
            throw new IllegalArgumentException(
                    "Solo il leader può indicare un successore");
        }
        member.getTeam().getMembers().remove(member);
        teamMemberRepository.delete(member);
    }

    @Override
    @Transactional
    public TeamDetailsDTO viewTeam(Long teamId, String userEmail) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", teamId));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", userEmail));

        TeamMember membership = teamMemberRepository.findByTeamIdAndUserId(teamId, user.getId())
                .orElseThrow(() -> new ForbiddenOperationException(
                        "L'utente non appartiene al team"));

        if (!membership.isLeader() && !membership.isMember()) {
            throw new ForbiddenOperationException("Solo il Team Leader o i membri possono visualizzare il team");
        }

        return toTeamDetails(team);
    }

    @Override
    @Transactional
    public TeamDetailsDTO viewTeam(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", teamId));

        return toTeamDetails(team);
    }

    private TeamDetailsDTO toTeamDetails(Team team) {
        UserSummaryDTO teamLeader = team.findLeader()
                .map(this::toUserSummary)
                .orElse(null);
        List<UserSummaryDTO> members = team.getMembers().stream()
                .filter(TeamMember::isMember)
                .map(this::toUserSummary)
                .toList();
        List<HackathonSummaryDTO> registeredHackathons = team.getRegistrations().stream()
                .map(HackathonRegistration::getHackathon)
                .filter(Objects::nonNull)
                .map(this::toHackathonSummary)
                .toList();

        return new TeamDetailsDTO(
                team.getId(),
                team.getName(),
                teamLeader,
                members,
                registeredHackathons);
    }

    @Override
    @Transactional
    public void expelMember(Long teamId, String leaderEmail, Long memberId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", teamId));

        User leader = userRepository.findByEmail(leaderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", leaderEmail));

        if (!teamMemberRepository.existsByTeamIdAndUserIdAndRole(teamId, leader.getId(), TeamRole.LEADER)) {
            throw new ForbiddenOperationException("Solo il team leader può espellere membri");
        }

        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("Il membro non appartiene al team"));

        if (member.isLeader()) {
            throw new ForbiddenOperationException(
                    "Il Team Leader non può essere espulso dal team");
        }

        team.getMembers().remove(member);
        teamMemberRepository.delete(member);
    }

    private UserSummaryDTO toUserSummary(TeamMember teamMember) {
        User user = teamMember.getUser();
        return new UserSummaryDTO(user.getId(), user.getName());
    }

    private HackathonSummaryDTO toHackathonSummary(Hackathon hackathon) {
        return new HackathonSummaryDTO(hackathon.getId(), hackathon.getName());
    }

}