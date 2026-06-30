package it.unicam.cs.ids.hackhub.controllers;

import it.unicam.cs.ids.hackhub.dto.support.CreateViolationReportRequestDTO;
import it.unicam.cs.ids.hackhub.dto.support.ViolationReportDTO;
import it.unicam.cs.ids.hackhub.model.ViolationReport;
import it.unicam.cs.ids.hackhub.service.interfaces.IMentorService;
import it.unicam.cs.ids.hackhub.service.interfaces.IViolationReportService;
import it.unicam.cs.ids.hackhub.service.mapper.ViolationReportMapper;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ViolationReportController {

    private final IViolationReportService violationReportService;
    private final IMentorService mentorService;
    private final ViolationReportMapper violationReportMapper;

    public ViolationReportController(IViolationReportService violationReportService,
                                     IMentorService mentorService,
                                     ViolationReportMapper violationReportMapper) {
        this.violationReportService = violationReportService;
        this.mentorService = mentorService;
        this.violationReportMapper = violationReportMapper;
    }

    @GetMapping("/api/hackathons/{hackathonId}/violations")
    @PreAuthorize("hasAnyRole('USER', 'ORGANIZER', 'MENTOR', 'JUDGE')")
    public List<ViolationReportDTO> viewReports(@PathVariable Long hackathonId,
                                                Authentication authentication) {
        return violationReportService.viewReportsForHackathon(
                hackathonId,
                authentication.getName());
    }

    /**
     * Segnala una violazione commessa da un team durante l'hackathon. Riservato
     * al mentore assegnato all'hackathon (identità nel path, validata dal
     * principal JWT).
     */
    @PostMapping("/api/mentors/{mentorId}/hackathons/{hackathonId}/violations")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(
            "hasRole('MENTOR') "
                    + "and @hackHubAuthorizationService.isMentorSelf(#mentorId, authentication.name) "
                    + "and @hackHubAuthorizationService.isAssignedMentor(#hackathonId, authentication.name)")
    public ViolationReportDTO reportViolation(
            @PathVariable Long mentorId,
            @PathVariable Long hackathonId,
            @Valid @RequestBody CreateViolationReportRequestDTO request) {
        ViolationReport report = mentorService.reportViolation(
                mentorId,
                hackathonId,
                request.teamId(),
                request.description());
        return violationReportMapper.toDto(report);
    }
}
