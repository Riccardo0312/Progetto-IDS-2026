package it.unicam.cs.ids.hackhub.controllers;

import it.unicam.cs.ids.hackhub.model.Team;
import it.unicam.cs.ids.hackhub.service.interfaces.ITeamService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final ITeamService teamService;

    public TeamController(ITeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Team createTeam(@RequestBody CreateTeamRequest request) {
        return teamService.createTeam(request.name(), request.creatorEmail());
    }

    public record CreateTeamRequest(String name, String creatorEmail) {
    }
}
