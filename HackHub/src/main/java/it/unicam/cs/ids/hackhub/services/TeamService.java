package it.unicam.cs.ids.hackhub.services;

import it.unicam.cs.ids.hackhub.model.Team;
import it.unicam.cs.ids.hackhub.model.TeamMember;
import it.unicam.cs.ids.hackhub.model.User;
import it.unicam.cs.ids.hackhub.model.repository.TeamMemberRepository;
import it.unicam.cs.ids.hackhub.model.repository.TeamRepository;
import it.unicam.cs.ids.hackhub.model.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class TeamService {
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    public TeamService(TeamRepository teamRepository, TeamMemberRepository teamMemberRepository, UserRepository userRepository) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Team createTeam(String name, String creatorEmail) {
        User creator = UserRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));

        if(teamRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Esiste già un team con questo nome");
        }

        if(teamMemberRepository.existsByUserId(creator.getId())) {
            throw new IllegalArgumentException("L'utente appartiene già a un team");
        }

        Team team = new Team();
        team.setName(name);
        team.setCreator(creator);

        Team savedTeam = teamRepository.save(team);

        TeamMember member = new TeamMember();
        member.setTeam(savedTeam);
        member.setUser(creator);
        teamMemberRepository.save(member);

        return savedTeam;
    }
}
