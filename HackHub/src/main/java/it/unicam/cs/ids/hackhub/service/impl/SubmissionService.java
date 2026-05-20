package it.unicam.cs.ids.hackhub.service.impl;

import it.unicam.cs.ids.hackhub.model.HackathonRegistration;
import it.unicam.cs.ids.hackhub.model.Submission;
import it.unicam.cs.ids.hackhub.model.repository.HackathonRegistrationRepository;
import it.unicam.cs.ids.hackhub.model.repository.SubmissionRepository;
import it.unicam.cs.ids.hackhub.service.interfaces.ISubmissionService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class SubmissionService implements ISubmissionService {

    private final HackathonRegistrationRepository registrationRepository;
    private final SubmissionRepository submissionRepository;

    public SubmissionService(HackathonRegistrationRepository registrationRepository,
                             SubmissionRepository submissionRepository) {
        this.registrationRepository = registrationRepository;
        this.submissionRepository = submissionRepository;
    }

    @Override
    @Transactional
    public Submission uploadSubmission(
            Long registrationId, String title, String description, String projectLink) {
        HackathonRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Registrazione non trovata"));

        if (isSubmissionDeadlineExpired(registration)) {
            throw new IllegalStateException("Deadline superata");
        }

        LocalDateTime now = LocalDateTime.now();
        Submission submission = new Submission();
        submission.setRegistration(registration);
        submission.setTitle(title);
        submission.setDescription(description);
        submission.setProjectLink(projectLink);
        submission.setUploadedAt(now);
        submission.setLastModifiedAt(now);

        return submissionRepository.save(submission);
    }

    @Override
    @Transactional
    public Submission updateSubmission(
            Long submissionId, String title, String description, String projectLink) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Sottomissione non trovata"));

        if (isSubmissionDeadlineExpired(submission.getRegistration())) {
            throw new IllegalStateException("Deadline superata");
        }

        submission.setTitle(title);
        submission.setDescription(description);
        submission.setProjectLink(projectLink);
        submission.setLastModifiedAt(LocalDateTime.now());

        return submissionRepository.save(submission);
    }

    private boolean isSubmissionDeadlineExpired(HackathonRegistration registration) {
        return LocalDate.now().isAfter(registration.getHackathon().getEndDate());
    }
}
