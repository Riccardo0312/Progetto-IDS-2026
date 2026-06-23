package it.unicam.cs.ids.hackhub.service.impl;

import it.unicam.cs.ids.hackhub.dto.hackathon.HackathonRegistrationsDTO;
import it.unicam.cs.ids.hackhub.dto.team.TeamSummaryDTO;
import it.unicam.cs.ids.hackhub.exception.ResourceNotFoundException;
import it.unicam.cs.ids.hackhub.model.Hackathon;
import it.unicam.cs.ids.hackhub.model.HackathonRegistration;
import it.unicam.cs.ids.hackhub.model.Team;
import it.unicam.cs.ids.hackhub.model.repository.HackathonRegistrationRepository;
import it.unicam.cs.ids.hackhub.model.repository.HackathonRepository;
import it.unicam.cs.ids.hackhub.model.repository.TeamRepository;
import it.unicam.cs.ids.hackhub.service.interfaces.IHackathonRegistrationService;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class HackathonService implements IHackathonRegistrationService {

    private final HackathonRepository hackathonRepository;
    private final TeamRepository teamRepository;
    private final HackathonRegistrationRepository hackathonRegistrationRepository;

    public HackathonService(HackathonRepository hackathonRepository, TeamRepository teamRepository,
                            HackathonRegistrationRepository hackathonRegistrationRepository) {
        this.hackathonRepository = hackathonRepository;
        this.teamRepository = teamRepository;
        this.hackathonRegistrationRepository = hackathonRegistrationRepository;
    }

    @Override
    public List<Hackathon> listAvailableHackathons() {
        return hackathonRepository.findAllByOrderByStartDateAsc();
    }

    @Override
    @Transactional
    public HackathonRegistration registerTeam(Long hackathonId, Long teamId) {
        Hackathon hackathon = hackathonRepository.findById(hackathonId)
                .orElseThrow(() -> new ResourceNotFoundException("Hackathon", hackathonId));

        // Allinea lo stato al tempo reale prima di applicare la guardia.
        hackathon.updateStatus();
        hackathon.ensureNewRegistrationsAllowed(LocalDate.now());

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", teamId));

        if (hackathonRegistrationRepository.existsByHackathonIdAndTeamId(hackathonId, teamId)) {
            throw new IllegalStateException("Team già registrato a questo hackathon");
        }

        HackathonRegistration registration = new HackathonRegistration();
        registration.setHackathon(hackathon);
        registration.setTeam(team);
        registration.setRegistrationDate(LocalDateTime.now());

        return hackathonRegistrationRepository.save(registration);
    }

    @Override
    @Transactional
    public HackathonRegistrationsDTO viewRegistrations(Long hackathonId) {
        if (!hackathonRepository.existsById(hackathonId)) {
            throw new ResourceNotFoundException("Hackathon", hackathonId);
        }

        List<TeamSummaryDTO> registeredTeams = hackathonRegistrationRepository
                .findByHackathonId(hackathonId)
                .stream()
                .map(HackathonRegistration::getTeam)
                .map(team -> new TeamSummaryDTO(team.getId(), team.getName()))
                .toList();

        return new HackathonRegistrationsDTO(registeredTeams.size(), registeredTeams);
    }
}