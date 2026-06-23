package it.unicam.cs.ids.hackhub.service.impl;

import it.unicam.cs.ids.hackhub.exception.ForbiddenOperationException;
import it.unicam.cs.ids.hackhub.exception.ResourceNotFoundException;
import it.unicam.cs.ids.hackhub.model.Hackathon;
import it.unicam.cs.ids.hackhub.model.HackathonRegistration;
import it.unicam.cs.ids.hackhub.model.Submission;
import it.unicam.cs.ids.hackhub.model.Team;
import it.unicam.cs.ids.hackhub.model.User;
import it.unicam.cs.ids.hackhub.model.repository.HackathonRegistrationRepository;
import it.unicam.cs.ids.hackhub.model.repository.SubmissionRepository;
import it.unicam.cs.ids.hackhub.model.repository.TeamMemberRepository;
import it.unicam.cs.ids.hackhub.model.repository.UserRepository;
import it.unicam.cs.ids.hackhub.service.interfaces.ISubmissionService;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

/**
 * Caso d'uso "Il team invia / modifica la sottomissione".
 *
 * <p>Authz: solo il leader o un membro del team proprietario (catena
 * registrazione -> team -> membership). L'azione e permessa solo mentre
 * l'hackathon e in stato {@code RUNNING} (guardia di stato), previo refresh
 * dello stato in base alla data corrente.
 *
 * <p>Ordine dei controlli: esistenza (404) -> membership (403) -> stato
 * hackathon (409) -> [solo upload] sottomissione gia presente (409).
 */
@Service
public class SubmissionService implements ISubmissionService {

    private final HackathonRegistrationRepository registrationRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;

    public SubmissionService(HackathonRegistrationRepository registrationRepository,
                             SubmissionRepository submissionRepository,
                             UserRepository userRepository,
                             TeamMemberRepository teamMemberRepository) {
        this.registrationRepository = registrationRepository;
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    @Override
    @Transactional
    public Submission uploadSubmission(
            Long registrationId, String userEmail, String title, String description, String projectLink) {
        HackathonRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registrazione", registrationId));

        ensureCallerIsTeamMember(registration.getTeam(), userEmail);
        ensureHackathonAcceptsSubmissions(registration.getHackathon());
        ensureTeamNotDisqualified(registration);

        if (registration.getSubmission() != null) {
            throw new IllegalStateException(
                    "La registrazione ha gia una sottomissione: usa la modifica");
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
            Long submissionId, String userEmail, String title, String description, String projectLink) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Sottomissione", submissionId));

        HackathonRegistration registration = submission.getRegistration();
        ensureCallerIsTeamMember(registration.getTeam(), userEmail);
        ensureHackathonAcceptsSubmissions(registration.getHackathon());
        ensureTeamNotDisqualified(registration);

        submission.setTitle(title);
        submission.setDescription(description);
        submission.setProjectLink(projectLink);
        submission.setLastModifiedAt(LocalDateTime.now());

        return submissionRepository.save(submission);
    }

    /**
     * Verifica che {@code userEmail} appartenga a un utente che e leader o
     * membro del team proprietario della sottomissione.
     */
    private void ensureCallerIsTeamMember(Team team, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", userEmail));
        if (!teamMemberRepository.existsByTeamIdAndUserId(team.getId(), user.getId())) {
            throw new ForbiddenOperationException(
                    "Solo i membri del team possono gestire la sottomissione");
        }
    }

    /**
     * Allinea lo stato dell'hackathon alla data corrente e verifica che la fase
     * permetta azioni sulle sottomissioni (solo {@code RUNNING}).
     */
    private void ensureHackathonAcceptsSubmissions(Hackathon hackathon) {
        hackathon.updateStatus();
        hackathon.ensureSubmissionActionsAllowed();
    }

    private void ensureTeamNotDisqualified(HackathonRegistration registration) {
        if (registration.isDisqualified()) {
            throw new ForbiddenOperationException(
                    "Il team è squalificato dall'hackathon e non può gestire la sottomissione");
        }
    }
}
