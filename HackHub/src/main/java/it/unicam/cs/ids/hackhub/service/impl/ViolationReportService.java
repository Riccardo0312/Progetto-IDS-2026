package it.unicam.cs.ids.hackhub.service.impl;

import it.unicam.cs.ids.hackhub.dto.support.ViolationReportDTO;
import it.unicam.cs.ids.hackhub.exception.ForbiddenOperationException;
import it.unicam.cs.ids.hackhub.exception.ResourceNotFoundException;
import it.unicam.cs.ids.hackhub.model.Hackathon;
import it.unicam.cs.ids.hackhub.model.User;
import it.unicam.cs.ids.hackhub.model.repository.HackathonRepository;
import it.unicam.cs.ids.hackhub.model.repository.UserRepository;
import it.unicam.cs.ids.hackhub.model.repository.ViolationReportRepository;
import it.unicam.cs.ids.hackhub.service.interfaces.IViolationReportService;
import it.unicam.cs.ids.hackhub.service.mapper.ViolationReportMapper;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ViolationReportService implements IViolationReportService {

    private final ViolationReportRepository violationReportRepository;
    private final HackathonRepository hackathonRepository;
    private final UserRepository userRepository;
    private final ViolationReportMapper violationReportMapper;

    public ViolationReportService(ViolationReportRepository violationReportRepository,
                                  HackathonRepository hackathonRepository,
                                  UserRepository userRepository,
                                  ViolationReportMapper violationReportMapper) {
        this.violationReportRepository = violationReportRepository;
        this.hackathonRepository = hackathonRepository;
        this.userRepository = userRepository;
        this.violationReportMapper = violationReportMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViolationReportDTO> viewReportsForHackathon(Long hackathonId, String userEmail) {
        Hackathon hackathon = hackathonRepository.findById(hackathonId)
                .orElseThrow(() -> new ResourceNotFoundException("Hackathon", hackathonId));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", userEmail));

        if (!canViewReports(hackathon, user)) {
            throw new ForbiddenOperationException(
                    "L'utente non può visualizzare le segnalazioni dell'hackathon");
        }

        return violationReportRepository.findByHackathonId(hackathonId).stream()
                .map(violationReportMapper::toDto)
                .toList();
    }

    private boolean canViewReports(Hackathon hackathon, User user) {
        return isOrganizerOfHackathon(hackathon, user)
                || isAssignedJudgeOfHackathon(hackathon, user)
                || isAssignedMentorOfHackathon(hackathon, user);
    }

    private boolean isOrganizerOfHackathon(Hackathon hackathon, User user) {
        return hackathon.getOrganizer() != null
                && Objects.equals(hackathon.getOrganizer().getId(), user.getId());
    }

    private boolean isAssignedJudgeOfHackathon(Hackathon hackathon, User user) {
        return hackathon.getJudge() != null
                && Objects.equals(hackathon.getJudge().getId(), user.getId());
    }

    private boolean isAssignedMentorOfHackathon(Hackathon hackathon, User user) {
        return hackathon.getMentors().stream()
                .anyMatch(mentor -> Objects.equals(mentor.getId(), user.getId()));
    }
}