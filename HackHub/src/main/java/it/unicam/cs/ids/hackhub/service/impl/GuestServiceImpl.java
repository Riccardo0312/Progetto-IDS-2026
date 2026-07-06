package it.unicam.cs.ids.hackhub.service.impl;

import it.unicam.cs.ids.hackhub.dto.hackathon.HackathonDetailDTO;
import it.unicam.cs.ids.hackhub.dto.hackathon.HackathonListItemDTO;
import it.unicam.cs.ids.hackhub.dto.hackathon.HackathonRegistrationsDTO;
import it.unicam.cs.ids.hackhub.dto.team.TeamSummaryDTO;
import it.unicam.cs.ids.hackhub.config.CurrentDateProvider;
import it.unicam.cs.ids.hackhub.exception.ResourceNotFoundException;
import it.unicam.cs.ids.hackhub.model.Hackathon;
import it.unicam.cs.ids.hackhub.model.HackathonStatus;
import it.unicam.cs.ids.hackhub.model.RegistrationStatus;
import it.unicam.cs.ids.hackhub.model.User;
import it.unicam.cs.ids.hackhub.model.repository.HackathonRegistrationRepository;
import it.unicam.cs.ids.hackhub.model.repository.HackathonRepository;
import it.unicam.cs.ids.hackhub.model.repository.UserRepository;
import java.util.List;
import it.unicam.cs.ids.hackhub.service.interfaces.IGuestService;
import it.unicam.cs.ids.hackhub.service.mapper.HackathonMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GuestServiceImpl implements IGuestService {

    private final HackathonRepository hackathonRepository;
    private final UserRepository userRepository;
    private final HackathonRegistrationRepository hackathonRegistrationRepository;
    private final HackathonMapper hackathonMapper;
    private final CurrentDateProvider currentDateProvider;

    public GuestServiceImpl(HackathonRepository hackathonRepository,
                            UserRepository userRepository,
                            HackathonRegistrationRepository hackathonRegistrationRepository,
                            HackathonMapper hackathonMapper,
                            CurrentDateProvider currentDateProvider) {
        this.hackathonRepository = hackathonRepository;
        this.userRepository = userRepository;
        this.hackathonRegistrationRepository = hackathonRegistrationRepository;
        this.hackathonMapper = hackathonMapper;
        this.currentDateProvider = currentDateProvider;
    }

    @Override
    public List<HackathonListItemDTO> getAllHackathons() {
        return hackathonRepository.findAllByOrderByStartDateAsc().stream()
                .peek(this::refreshHackathonStatus)
                .map(hackathonMapper::toListItem)
                .toList();
    }

    @Override
    public List<HackathonListItemDTO> getHackathonsByStatus(HackathonStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Lo stato non può essere null");
        }
        return hackathonRepository.findAllByOrderByStartDateAsc().stream()
                .peek(this::refreshHackathonStatus)
                .filter(hackathon -> hackathon.getStatus() == status)
                .map(hackathonMapper::toListItem)
                .toList();
    }

    @Override
    public HackathonDetailDTO getHackathonById(Long hackathonId) {
        if (hackathonId == null) {
            throw new IllegalArgumentException("L'ID non può essere null");
        }
        Hackathon hackathon = hackathonRepository.findById(hackathonId)
                .orElseThrow(() -> new ResourceNotFoundException("Hackathon", hackathonId));
        refreshHackathonStatus(hackathon);
        return hackathonMapper.toDetail(hackathon);
    }

    private void refreshHackathonStatus(Hackathon hackathon) {
        hackathon.updateStatus(currentDateProvider.today());
    }

    @Transactional
    @Override
    public User register(User user) {
        if (user == null) {
            throw new IllegalArgumentException("L'utente non può essere null");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException(
                    "Email già registrata: " + user.getEmail());
        }
        return userRepository.save(user);
    }

    @Override
    public HackathonRegistrationsDTO getHackathonRegistrations(Long hackathonId) {
        if (!hackathonRepository.existsById(hackathonId)) {
            throw new ResourceNotFoundException("Hackathon", hackathonId);
        }
        List<TeamSummaryDTO> teams = hackathonRegistrationRepository
                .findByHackathonIdAndStatus(hackathonId, RegistrationStatus.ACTIVE).stream()
                .map(r -> new TeamSummaryDTO(r.getTeam().getId(), r.getTeam().getName()))
                .toList();
        return new HackathonRegistrationsDTO(teams.size(), teams);
    }
}
