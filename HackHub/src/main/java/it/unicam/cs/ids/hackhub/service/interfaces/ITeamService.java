package it.unicam.cs.ids.hackhub.service.interfaces;

import it.unicam.cs.ids.hackhub.model.Team;

public interface ITeamService {
    Team createTeam(String name, String creatorEmail);
}
