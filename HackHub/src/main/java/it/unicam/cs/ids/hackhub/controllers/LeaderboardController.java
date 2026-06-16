package it.unicam.cs.ids.hackhub.controllers;

import it.unicam.cs.ids.hackhub.dto.leaderboard.LeaderboardResponseDTO;
import it.unicam.cs.ids.hackhub.service.interfaces.ILeaderboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/hackathons/{hackathonId}/leaderboard")
@PreAuthorize("isAuthenticated()")
public class LeaderboardController {

    private final ILeaderboardService leaderboardService;

    public LeaderboardController(ILeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping
    public LeaderboardResponseDTO getLeaderboard(@PathVariable Long hackathonId) {
        return leaderboardService.getLeaderboard(hackathonId);
    }

}