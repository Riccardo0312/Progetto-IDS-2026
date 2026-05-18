package it.unicam.cs.ids.hackhub.service.interfaces;

import it.unicam.cs.ids.hackhub.model.Evaluation;
import it.unicam.cs.ids.hackhub.model.Submission;
import java.util.List;

public interface IJudgeService {

	List<Submission> getAssignedHackathonSubmissions(Long judgeId, Long hackathonId);

	Evaluation evaluateSubmission(
			Long judgeId, Long hackathonId, Long submissionId, String judgment, int score);

}
