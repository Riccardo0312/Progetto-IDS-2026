package it.unicam.cs.ids.hackhub.controllers;

import it.unicam.cs.ids.hackhub.dto.hackathon.HackathonRegistrationsDTO;
import it.unicam.cs.ids.hackhub.service.interfaces.IGuestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hackathons")
public class GuestController {

    private final IGuestService guestService;

    public GuestController(IGuestService guestService) {
        this.guestService = guestService;
    }

    @GetMapping("/{hackathonId}/registrations")
    public HackathonRegistrationsDTO getRegistrations(@PathVariable Long hackathonId) {
        return guestService.getHackathonRegistrations(hackathonId);
    }
}
