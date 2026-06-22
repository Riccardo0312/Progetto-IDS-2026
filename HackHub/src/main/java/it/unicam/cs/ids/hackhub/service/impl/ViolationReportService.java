package it.unicam.cs.ids.hackhub.service.impl;

import it.unicam.cs.ids.hackhub.model.User;
import it.unicam.cs.ids.hackhub.model.ViolationReport;
import it.unicam.cs.ids.hackhub.model.repository.ViolationReportRepository;
import it.unicam.cs.ids.hackhub.model.repository.TeamMemberRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ViolationReportService {

    private final ViolationReportRepository violationReportRepository;
    private final TeamMemberRepository teamMemberRepository;

    public ViolationReportService(ViolationReportRepository violationReportRepository,
                                  TeamMemberRepository teamMemberRepository) {
        this.violationReportRepository = violationReportRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    @Transactional
    public List<ViolationReport> viewReportsForHackathon(Long hackathonId, User user) {
        // Precondizione: l'utente deve essere staff (organizzatore, mentore, giudice) o membro del team
        boolean isStaff = user.getRole().isStaff();
        boolean isTeamMember = teamMemberRepository.existsByUserId(user.getId());

        if (!isStaff && !isTeamMember) {
            throw new IllegalStateException("L'utente non può visualizzare le segnalazioni");
        }

        // Recupera le segnalazioni relative all'hackathon
        return violationReportRepository.findByHackathonId(hackathonId);
    }
}
