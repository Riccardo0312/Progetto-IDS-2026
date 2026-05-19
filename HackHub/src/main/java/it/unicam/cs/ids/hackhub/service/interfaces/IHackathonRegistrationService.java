package it.unicam.cs.ids.hackhub.service.interfaces;

import it.unicam.cs.ids.hackhub.model.Hackathon;
import it.unicam.cs.ids.hackhub.model.HackathonRegistration;

import java.util.List;

public interface IHackathonRegistrationService {
    List<Hackathon> listAvailableHackathons();
    HackathonRegistration registerTeam(Long hackathonId, Long teamId);
}
