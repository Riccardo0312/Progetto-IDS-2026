package it.unicam.cs.ids.hackhub.service.validation;

import it.unicam.cs.ids.hackhub.model.Hackathon;
import it.unicam.cs.ids.hackhub.model.Judge;
import it.unicam.cs.ids.hackhub.model.Submission;

public record EvaluationRequestContext(
		Long judgeId,
		Long hackathonId,
		Long submissionId,
		Judge judge,
		Hackathon hackathon,
		Submission submission,
		String judgment,
		int score) {}
