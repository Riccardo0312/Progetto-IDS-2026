package it.unicam.cs.ids.hackhub.service.interfaces;

import it.unicam.cs.ids.hackhub.model.Submission;

public interface ISubmissionService {
    Submission uploadSubmission(Long registrationId, String title, String description, String projectLink);

    Submission updateSubmission(Long submissionId, String title, String description, String projectLink);
}
