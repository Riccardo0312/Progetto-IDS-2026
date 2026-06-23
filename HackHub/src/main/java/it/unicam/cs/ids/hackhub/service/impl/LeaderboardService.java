package it.unicam.cs.ids.hackhub.service.impl;

import it.unicam.cs.ids.hackhub.dto.leaderboard.LeaderboardEntryDTO;
import it.unicam.cs.ids.hackhub.dto.leaderboard.LeaderboardResponseDTO;
import it.unicam.cs.ids.hackhub.exception.ForbiddenOperationException;
import it.unicam.cs.ids.hackhub.exception.ResourceNotFoundException;
import it.unicam.cs.ids.hackhub.model.Evaluation;
import it.unicam.cs.ids.hackhub.model.Hackathon;
import it.unicam.cs.ids.hackhub.model.HackathonStatus;
import it.unicam.cs.ids.hackhub.model.repository.EvaluationRepository;
import it.unicam.cs.ids.hackhub.model.repository.HackathonRepository;
import it.unicam.cs.ids.hackhub.service.interfaces.ILeaderboardService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LeaderboardService implements ILeaderboardService {

    private final HackathonRepository hackathonRepository;
    private final EvaluationRepository evaluationRepository;

    public LeaderboardService(
            HackathonRepository hackathonRepository,
            EvaluationRepository evaluationRepository) {
        this.hackathonRepository = hackathonRepository;
        this.evaluationRepository = evaluationRepository;
    }

    @Override
    public LeaderboardResponseDTO getLeaderboard(Long hackathonId) {
        Hackathon hackathon = hackathonRepository
                .findById(hackathonId)
                .orElseThrow(() -> new ResourceNotFoundException("Hackathon", hackathonId));

        hackathon.updateStatus();
        validateLeaderboardAvailable(hackathon);

        List<Evaluation> evaluations =
                evaluationRepository.findByHackathonIdOrderByScoreDesc(hackathonId);

        List<LeaderboardEntryDTO> entries = buildEntries(evaluations);

        return new LeaderboardResponseDTO(hackathon.getId(), hackathon.getName(), entries);
    }


    private void validateLeaderboardAvailable(Hackathon hackathon) {
        HackathonStatus status = hackathon.getStatus();
        if (status != HackathonStatus.EVALUATION && status != HackathonStatus.CONCLUDED) {
            throw new ForbiddenOperationException(
                    "La classifica dell'hackathon " + hackathon.getId()
                            + " non è ancora disponibile (stato attuale: " + status + ")");
        }
    }


    private List<LeaderboardEntryDTO> buildEntries(List<Evaluation> evaluations) {
        List<LeaderboardEntryDTO> entries = new ArrayList<>();
        int position = 1;
        // filter out disqualified teams (ADR 0005)
        List<Evaluation> active = evaluations.stream()
                .filter(e -> !e.getSubmission().getRegistration().isDisqualified())
                .toList();
        for (int i = 0; i < active.size(); i++) {
            Evaluation eval = active.get(i);

            if (i > 0 && eval.getScore() < active.get(i - 1).getScore()) {
                position = i + 1;
            }

            Long teamId   = eval.getSubmission().getRegistration().getTeam().getId();
            String teamName = eval.getSubmission().getRegistration().getTeam().getName();
            Long submissionId    = eval.getSubmission().getId();
            String submissionTitle = eval.getSubmission().getTitle();

            entries.add(new LeaderboardEntryDTO(
                    position, teamId, teamName, submissionId, submissionTitle, eval.getScore()));
        }
        return entries;
    }
}