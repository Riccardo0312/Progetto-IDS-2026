package it.unicam.cs.ids.hackhub.service.impl;

import it.unicam.cs.ids.hackhub.config.CurrentDateProvider;
import it.unicam.cs.ids.hackhub.dto.leaderboard.LeaderboardEntryDTO;
import it.unicam.cs.ids.hackhub.dto.leaderboard.LeaderboardResponseDTO;
import it.unicam.cs.ids.hackhub.exception.ForbiddenOperationException;
import it.unicam.cs.ids.hackhub.exception.ResourceNotFoundException;
import it.unicam.cs.ids.hackhub.model.Evaluation;
import it.unicam.cs.ids.hackhub.model.Hackathon;
import it.unicam.cs.ids.hackhub.model.HackathonRegistration;
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
    private final CurrentDateProvider currentDateProvider;

    public LeaderboardService(
            HackathonRepository hackathonRepository,
            EvaluationRepository evaluationRepository,
            CurrentDateProvider currentDateProvider) {
        this.hackathonRepository = hackathonRepository;
        this.evaluationRepository = evaluationRepository;
        this.currentDateProvider = currentDateProvider;
    }

    @Override
    public LeaderboardResponseDTO getLeaderboard(Long hackathonId) {
        Hackathon hackathon = hackathonRepository
                .findById(hackathonId)
                .orElseThrow(() -> new ResourceNotFoundException("Hackathon", hackathonId));

        hackathon.updateStatus(currentDateProvider.today());
        validateLeaderboardAvailable(hackathon);
        validateFinalScoresAvailable(hackathon);

        List<Evaluation> evaluations =
                evaluationRepository.findByHackathonIdOrderByScoreDesc(hackathonId);

        List<LeaderboardEntryDTO> entries = buildEntries(evaluations);

        return new LeaderboardResponseDTO(hackathon.getId(), hackathon.getName(), entries);
    }


    private void validateFinalScoresAvailable(Hackathon hackathon) {
        boolean hasEligibleTeamWithoutFinalScore = hackathon.getRegistrations().stream()
                .filter(registration -> !registration.isDisqualified())
                .anyMatch(this::doesNotHaveFinalScore);

        if (hasEligibleTeamWithoutFinalScore) {
            throw new ForbiddenOperationException(
                    "La classifica dell'hackathon " + hackathon.getId()
                            + " non è ancora definitiva: ci sono sottomissioni non valutate");
        }
    }


    private boolean doesNotHaveFinalScore(HackathonRegistration registration) {
        return registration.getSubmission() == null
                || registration.getSubmission().getEvaluation() == null;
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
