package it.unicam.cs.ids.hackhub.service.impl;

import it.unicam.cs.ids.hackhub.dto.hackathon.TeamRegistrationResponseDTO;
import it.unicam.cs.ids.hackhub.config.CurrentDateProvider;
import it.unicam.cs.ids.hackhub.exception.ResourceNotFoundException;
import it.unicam.cs.ids.hackhub.model.Hackathon;
import it.unicam.cs.ids.hackhub.model.HackathonRegistration;
import it.unicam.cs.ids.hackhub.model.Team;
import it.unicam.cs.ids.hackhub.model.repository.HackathonRegistrationRepository;
import it.unicam.cs.ids.hackhub.model.repository.HackathonRepository;
import it.unicam.cs.ids.hackhub.model.repository.TeamRepository;
import it.unicam.cs.ids.hackhub.service.interfaces.IHackathonRegistrationService;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class HackathonService implements IHackathonRegistrationService {

    private final HackathonRepository hackathonRepository;
    private final TeamRepository teamRepository;
    private final HackathonRegistrationRepository hackathonRegistrationRepository;
    private final CurrentDateProvider currentDateProvider;

    public HackathonService(HackathonRepository hackathonRepository, TeamRepository teamRepository,
                            HackathonRegistrationRepository hackathonRegistrationRepository,
                            CurrentDateProvider currentDateProvider) {
        this.hackathonRepository = hackathonRepository;
        this.teamRepository = teamRepository;
        this.hackathonRegistrationRepository = hackathonRegistrationRepository;
        this.currentDateProvider = currentDateProvider;
    }

    @Override
    @Transactional
    public TeamRegistrationResponseDTO registerTeam(Long hackathonId, Long teamId) {
        Hackathon hackathon = hackathonRepository.findById(hackathonId)
                .orElseThrow(() -> new ResourceNotFoundException("Hackathon", hackathonId));

        // Allinea lo stato alla data applicativa prima di applicare la guardia.
        hackathon.updateStatus(currentDateProvider.today());
        hackathon.ensureNewRegistrationsAllowed(currentDateProvider.today());

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", teamId));

        if (hackathonRegistrationRepository.existsByHackathonIdAndTeamId(hackathonId, teamId)) {
            throw new IllegalStateException("Team già registrato a questo hackathon");
        }

        HackathonRegistration registration = new HackathonRegistration();
        registration.setHackathon(hackathon);
        registration.setTeam(team);
        registration.setRegistrationDate(LocalDateTime.now());

        HackathonRegistration saved = hackathonRegistrationRepository.save(registration);

        return new TeamRegistrationResponseDTO(
                saved.getId(),
                hackathon.getId(),
                team.getId(),
                saved.getStatus(),
                saved.getRegistrationDate());
    }
}
