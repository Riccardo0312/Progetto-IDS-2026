package it.unicam.cs.ids.hackhub.controllers;

import it.unicam.cs.ids.hackhub.dto.team.DeleteTeamRequestDTO;
import it.unicam.cs.ids.hackhub.dto.team.LeaveTeamRequestDTO;
import it.unicam.cs.ids.hackhub.dto.team.ViewTeamRequestDTO;
import it.unicam.cs.ids.hackhub.model.Team;
import it.unicam.cs.ids.hackhub.service.interfaces.ITeamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @PostMapping("/{teamId}/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveTeam(@PathVariable Long teamId,
                          @Valid @RequestBody LeaveTeamRequestDTO request) {
        teamService.leaveTeam(teamId, request.userEmail(), request.successorEmail());
    }

    @DeleteMapping("/{teamId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTeam(@PathVariable Long teamId,
                           @Valid @RequestBody DeleteTeamRequestDTO request) {
        teamService.deleteTeam(teamId, request.userEmail());
    }

    @PostMapping("/{teamId}/view")
    public Team viewTeam(@PathVariable Long teamId,
                         @Valid @RequestBody ViewTeamRequestDTO request) {
        // Il service restituisce direttamente l'entità Team
        return teamService.viewTeamByUser(request.userEmail());
    }

    public record CreateTeamRequest(String name, String creatorEmail) {}
}
