package it.unicam.cs.ids.hackhub.controllers;

import it.unicam.cs.ids.hackhub.dto.hackathon.RegisterTeamRequestDTO;
import it.unicam.cs.ids.hackhub.dto.hackathon.TeamRegistrationResponseDTO;
import it.unicam.cs.ids.hackhub.service.interfaces.IHackathonRegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint REST per il caso d'uso "Iscrizione team a hackathon".
 *
 * <p>La consultazione pubblica delle iscrizioni ({@code GET}) è esposta da
 * {@code GuestController}; qui vive solo la creazione ({@code POST}), riservata
 * al leader del team da iscrivere.
 */
@RestController
@RequestMapping("/api/hackathons/{hackathonId}/registrations")
public class HackathonRegistrationController {

    private final IHackathonRegistrationService hackathonRegistrationService;

    public HackathonRegistrationController(
            IHackathonRegistrationService hackathonRegistrationService) {
        this.hackathonRegistrationService = hackathonRegistrationService;
    }

    /**
     * Iscrive un team all'hackathon. Solo il leader del team indicato nel corpo
     * può eseguire l'operazione.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@hackHubAuthorizationService.isTeamLeader(#request.teamId(), authentication.name)")
    public TeamRegistrationResponseDTO registerTeam(
            @PathVariable Long hackathonId,
            @Valid @RequestBody RegisterTeamRequestDTO request) {
        return hackathonRegistrationService.registerTeam(hackathonId, request.teamId());
    }
}
