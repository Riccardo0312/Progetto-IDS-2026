package it.unicam.cs.ids.hackhub.controllers;

import it.unicam.cs.ids.hackhub.dto.hackathon.HackathonDetailDTO;
import it.unicam.cs.ids.hackhub.dto.hackathon.HackathonListItemDTO;
import it.unicam.cs.ids.hackhub.dto.hackathon.HackathonRegistrationsDTO;
import it.unicam.cs.ids.hackhub.model.HackathonStatus;
import it.unicam.cs.ids.hackhub.service.interfaces.IGuestService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint REST pubblici per il Visitatore (utente non autenticato).
 *
 * <p>Espone la consultazione del catalogo hackathon: lista (con filtro
 * opzionale per stato), dettaglio singolo e iscrizioni. L'accesso pubblico è
 * configurato in {@code SecurityConfig} via {@code permitAll}.
 */
@RestController
@RequestMapping("/api/hackathons")
public class GuestController {

    private final IGuestService guestService;

    public GuestController(IGuestService guestService) {
        this.guestService = guestService;
    }

    /**
     * Lista hackathon ordinata per data di inizio crescente. Se {@code status}
     * è valorizzato, filtra per lo stato indicato; altrimenti restituisce tutti.
     */
    @GetMapping
    public List<HackathonListItemDTO> getHackathons(
            @RequestParam(required = false) HackathonStatus status) {
        return status == null
                ? guestService.getAllHackathons()
                : guestService.getHackathonsByStatus(status);
    }

    @GetMapping("/{hackathonId}")
    public HackathonDetailDTO getHackathon(@PathVariable Long hackathonId) {
        return guestService.getHackathonById(hackathonId);
    }

    @GetMapping("/{hackathonId}/registrations")
    public HackathonRegistrationsDTO getRegistrations(@PathVariable Long hackathonId) {
        return guestService.getHackathonRegistrations(hackathonId);
    }
}
