package it.unicam.cs.ids.hackhub.dto.leaderboard;

public record LeaderboardEntryDTO(
		int position,
		Long teamId,
		String teamName,
		Long submissionId,
		String submissionTitle,
		int finalScore) {
}
