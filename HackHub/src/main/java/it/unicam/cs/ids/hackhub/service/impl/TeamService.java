package it.unicam.cs.ids.hackhub.service.impl;

import it.unicam.cs.ids.hackhub.exception.ForbiddenOperationException;
import it.unicam.cs.ids.hackhub.model.Team;
import it.unicam.cs.ids.hackhub.model.TeamMember;
import it.unicam.cs.ids.hackhub.model.TeamRole;
import it.unicam.cs.ids.hackhub.model.User;
import it.unicam.cs.ids.hackhub.model.repository.TeamMemberRepository;
import it.unicam.cs.ids.hackhub.model.repository.TeamRepository;
import it.unicam.cs.ids.hackhub.model.repository.UserRepository;
import it.unicam.cs.ids.hackhub.service.interfaces.ITeamService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class TeamService implements ITeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    public TeamService(TeamRepository teamRepository,
                       TeamMemberRepository teamMemberRepository,
                       UserRepository userRepository) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Team createTeam(String name, String creatorEmail) {
        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));

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
                .orElseThrow(() -> new IllegalArgumentException("Team non trovato"));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));

        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, user.getId())
                .orElseThrow(() -> new ForbiddenOperationException(
                        "L'utente non appartiene al team"));

        if (member.isLeader()) {
            leaveAsLeader(team, member, successorEmail);
        } else {
            leaveAsMember(member, successorEmail);
        }
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
                .orElseThrow(() -> new IllegalArgumentException("Successore non trovato"));

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
        teamMemberRepository.delete(member);
    }
}
