package it.unicam.cs.ids.hackhub.controllers;

import it.unicam.cs.ids.hackhub.dto.team.LeaveTeamRequestDTO;
import it.unicam.cs.ids.hackhub.dto.team.TeamDetailsDTO;
import it.unicam.cs.ids.hackhub.model.Team;
import it.unicam.cs.ids.hackhub.service.interfaces.ITeamService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint REST dei team.
 *
 * <p>L'identità dell'attore proviene sempre dal principal JWT
 * ({@code authentication.getName()} = email), mai dal body. Le autorizzazioni
 * sono espresse con {@code @PreAuthorize} + {@code HackHubAuthorizationService}.
 */
@RestController
@RequestMapping("/api/teams")
public class TeamController {

	private final ITeamService teamService;

	public TeamController(ITeamService teamService) {
		this.teamService = teamService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasRole('USER')")
	public Team createTeam(
			@Valid @RequestBody CreateTeamRequest request, Authentication authentication) {
		return teamService.createTeam(request.name(), authentication.getName());
	}

	@PostMapping("/{teamId}/leave")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("@hackHubAuthorizationService.isTeamMember(#teamId, authentication.name)")
	public void leaveTeam(
			@PathVariable Long teamId,
			@Valid @RequestBody LeaveTeamRequestDTO request,
			Authentication authentication) {
		teamService.leaveTeam(teamId, authentication.getName(), request.successorEmail());
	}

	@DeleteMapping("/{teamId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("@hackHubAuthorizationService.isTeamLeader(#teamId, authentication.name)")
	public void deleteTeam(@PathVariable Long teamId, Authentication authentication) {
		teamService.deleteTeam(teamId, authentication.getName());
	}

	@PostMapping("/{teamId}/view")
	@PreAuthorize("@hackHubAuthorizationService.isTeamMember(#teamId, authentication.name)")
	public TeamDetailsDTO viewTeam(@PathVariable Long teamId, Authentication authentication) {
		return teamService.viewTeam(teamId, authentication.getName());
	}

	/** Solo il nome: il creatore è il principal JWT. */
	public record CreateTeamRequest(@NotBlank String name) {}
}
