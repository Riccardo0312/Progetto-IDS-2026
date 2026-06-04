package it.unicam.cs.ids.hackhub.service.interfaces;

import it.unicam.cs.ids.hackhub.model.Submission;

public interface ISubmissionService {
    Submission uploadSubmission(
            Long registrationId, String userEmail, String title, String description, String projectLink);

    Submission updateSubmission(
            Long submissionId, String userEmail, String title, String description, String projectLink);
}
