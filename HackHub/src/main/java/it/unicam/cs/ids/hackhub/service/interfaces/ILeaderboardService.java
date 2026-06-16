package it.unicam.cs.ids.hackhub.service.interfaces;

import it.unicam.cs.ids.hackhub.dto.leaderboard.LeaderboardResponseDTO;

public interface ILeaderboardService {
    LeaderboardResponseDTO getLeaderboard(Long hackathonId);
}
