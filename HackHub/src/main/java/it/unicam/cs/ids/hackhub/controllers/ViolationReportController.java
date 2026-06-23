package it.unicam.cs.ids.hackhub.controllers;

import it.unicam.cs.ids.hackhub.dto.support.ViolationReportDTO;
import it.unicam.cs.ids.hackhub.service.interfaces.IViolationReportService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ViolationReportController {

    private final IViolationReportService violationReportService;

    public ViolationReportController(IViolationReportService violationReportService) {
        this.violationReportService = violationReportService;
    }

    @GetMapping("/api/hackathons/{hackathonId}/violations")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'MENTOR', 'JUDGE')")
    public List<ViolationReportDTO> viewReports(@PathVariable Long hackathonId,
                                                Authentication authentication) {
        return violationReportService.viewReportsForHackathon(
                hackathonId,
                authentication.getName());
    }
}