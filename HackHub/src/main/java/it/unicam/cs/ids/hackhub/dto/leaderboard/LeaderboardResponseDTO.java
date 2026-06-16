package it.unicam.cs.ids.hackhub.dto.leaderboard;
import java.util.List;


public class LeaderboardResponseDTO {
    public record LeaderboardResponseDTO(
            Long hackathonId,
            String hackathonName,
            List<LeaderboardEntryDTO> entries) {}

}
