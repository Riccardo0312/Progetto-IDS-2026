package it.unicam.cs.ids.hackhub.service.impl;

import it.unicam.cs.ids.hackhub.model.HackathonRegistration;
import it.unicam.cs.ids.hackhub.model.Submission;
import it.unicam.cs.ids.hackhub.model.repository.HackathonRegistrationRepository;
import it.unicam.cs.ids.hackhub.model.repository.SubmissionRepository;
import it.unicam.cs.ids.hackhub.service.interfaces.ISubmissionService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    public Submission uploadSubmission(Long registrationId, MultipartFile file) throws IOException {
        HackathonRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Registrazione non trovata"));

        if (LocalDateTime.now().isAfter(registration.getHackathon().getDeadline())) {
            throw new IllegalStateException("Deadline superata");
        }

        Submission submission = new Submission();
        submission.setTeam(registration.getTeam());
        submission.setHackathon(registration.getHackathon());
        submission.setFileData(file.getBytes());
        submission.setUploadDate(LocalDateTime.now());

        return submissionRepository.save(submission);
    }

    @Override
    @Transactional
    public Submission updateSubmission(Long submissionId, MultipartFile file) throws IOException {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Sottomissione non trovata"));

        if (LocalDateTime.now().isAfter(submission.getHackathon().getDeadline())) {
            throw new IllegalStateException("Deadline superata");
        }

        submission.setFileData(file.getBytes());
        submission.setUploadDate(LocalDateTime.now());
        return submissionRepository.save(submission);
    }
}
