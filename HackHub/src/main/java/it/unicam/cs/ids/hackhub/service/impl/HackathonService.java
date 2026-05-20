package it.unicam.cs.ids.hackhub.service.impl;

import it.unicam.cs.ids.hackhub.model.*;
import it.unicam.cs.ids.hackhub.model.repository.HackathonRegistrationRepository;
import it.unicam.cs.ids.hackhub.model.repository.HackathonRepository;
import it.unicam.cs.ids.hackhub.model.repository.TeamRepository;
import it.unicam.cs.ids.hackhub.service.interfaces.IHackathonRegistrationService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
                .orElseThrow(() -> new IllegalArgumentException("Hackathon non trovato"));

        if (hackathon.getStatus() == HackathonStatus.CONCLUDED) {
            throw new IllegalStateException("Hackathon già concluso");
        }

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team non trovato"));

        if (hackathonRegistrationRepository.existsByHackathonIdAndTeamId(hackathonId, teamId)) {
            throw new IllegalStateException("Team già registrato a questo hackathon");
        }

        HackathonRegistration registration = new HackathonRegistration();
        registration.setHackathon(hackathon);
        registration.setTeam(team);
        registration.setRegistrationDate(LocalDateTime.now());

        return hackathonRegistrationRepository.save(registration);
    }
}
