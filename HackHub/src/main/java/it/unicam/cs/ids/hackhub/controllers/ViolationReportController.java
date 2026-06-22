package it.unicam.cs.ids.hackhub.controllers;

import it.unicam.cs.ids.hackhub.dto.support.ViolationReportDTO;
import it.unicam.cs.ids.hackhub.model.User;
import it.unicam.cs.ids.hackhub.model.ViolationReport;
import it.unicam.cs.ids.hackhub.model.repository.UserRepository;
import it.unicam.cs.ids.hackhub.service.impl.ViolationReportService;
import java.security.Principal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ViolationReportController {

    private final ViolationReportService violationReportService;
    private final UserRepository userRepository;

    public ViolationReportController(ViolationReportService violationReportService,
                                     UserRepository userRepository) {
        this.violationReportService = violationReportService;
        this.userRepository = userRepository;
    }

    @GetMapping("/api/hackathons/{hackathonId}/violations")
    public ResponseEntity<List<ViolationReportDTO>> viewReports(@PathVariable Long hackathonId,
                                                                Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));

        // Recupera le segnalazioni direttamente dal metodo esistente
        List<ViolationReport> reports = violationReportService.viewReportsForHackathon(hackathonId, user);

        // Mappa in DTO inline
        List<ViolationReportDTO> dtoList = reports.stream()
                .map(r -> new ViolationReportDTO(
                        r.getId(),
                        r.getDescription(),
                        r.getReportedAt(),
                        r.getTeam().getName(),
                        r.getMentor().getName()
                ))
                .toList();

        return ResponseEntity.ok(dtoList);
    }
}