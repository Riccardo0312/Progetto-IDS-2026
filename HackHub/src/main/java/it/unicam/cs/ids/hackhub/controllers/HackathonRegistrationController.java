package it.unicam.cs.ids.hackhub.controllers;

import it.unicam.cs.ids.hackhub.dto.hackathon.HackathonRegistrationsDTO;
import it.unicam.cs.ids.hackhub.service.interfaces.IHackathonRegistrationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hackathons/{hackathonId}/registrations")
public class HackathonRegistrationController {

    private final IHackathonRegistrationService hackathonRegistrationService;

    public HackathonRegistrationController(
            IHackathonRegistrationService hackathonRegistrationService) {
        this.hackathonRegistrationService = hackathonRegistrationService;
    }

    @GetMapping
    public HackathonRegistrationsDTO viewRegistrations(@PathVariable Long hackathonId) {
        return hackathonRegistrationService.viewRegistrations(hackathonId);
    }

}
