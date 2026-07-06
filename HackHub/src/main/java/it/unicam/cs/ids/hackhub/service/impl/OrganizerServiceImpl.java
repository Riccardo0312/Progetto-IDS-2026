package it.unicam.cs.ids.hackhub.service.impl;

import it.unicam.cs.ids.hackhub.dto.hackathon.HackathonResponseDTO;
import it.unicam.cs.ids.hackhub.dto.hackathon.UpdateHackathonRequestDTO;
import it.unicam.cs.ids.hackhub.dto.prize.PrizeDisbursementResponseDTO;
import it.unicam.cs.ids.hackhub.dto.staff.StaffMemberSummaryDTO;
import it.unicam.cs.ids.hackhub.config.CurrentDateProvider;
import it.unicam.cs.ids.hackhub.exception.ForbiddenOperationException;
import it.unicam.cs.ids.hackhub.exception.InvalidHackathonStateException;
import it.unicam.cs.ids.hackhub.exception.PrizeAlreadyDisbursedException;
import it.unicam.cs.ids.hackhub.exception.PrizeDisbursementFailedException;
import it.unicam.cs.ids.hackhub.exception.ResourceNotFoundException;
import it.unicam.cs.ids.hackhub.model.Hackathon;
import it.unicam.cs.ids.hackhub.model.HackathonRegistration;
import it.unicam.cs.ids.hackhub.model.HackathonStatus;
import it.unicam.cs.ids.hackhub.model.Judge;
import it.unicam.cs.ids.hackhub.model.Mentor;
import it.unicam.cs.ids.hackhub.model.Organizer;
import it.unicam.cs.ids.hackhub.model.PaymentResult;
import it.unicam.cs.ids.hackhub.model.PrizeDisbursement;
import it.unicam.cs.ids.hackhub.model.PrizeDisbursementStatus;
import it.unicam.cs.ids.hackhub.model.RegistrationStatus;
import it.unicam.cs.ids.hackhub.model.Submission;
import it.unicam.cs.ids.hackhub.model.Team;
import it.unicam.cs.ids.hackhub.model.User;
import it.unicam.cs.ids.hackhub.model.repository.HackathonRegistrationRepository;
import it.unicam.cs.ids.hackhub.model.repository.HackathonRepository;
import it.unicam.cs.ids.hackhub.model.repository.JudgeRepository;
import it.unicam.cs.ids.hackhub.model.repository.MentorRepository;
import it.unicam.cs.ids.hackhub.model.repository.OrganizerRepository;
import it.unicam.cs.ids.hackhub.model.repository.PrizeDisbursementRepository;
import it.unicam.cs.ids.hackhub.service.interfaces.IOrganizerService;
import it.unicam.cs.ids.hackhub.service.interfaces.IPaymentGateway;
import it.unicam.cs.ids.hackhub.service.mapper.HackathonMapper;
import it.unicam.cs.ids.hackhub.service.mapper.PrizeDisbursementMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrganizerServiceImpl implements IOrganizerService {

    private final HackathonRepository hackathonRepository;
    private final OrganizerRepository organizerRepository;
    private final JudgeRepository judgeRepository;
    private final MentorRepository mentorRepository;
    private final PrizeDisbursementRepository prizeDisbursementRepository;
    private final HackathonRegistrationRepository hackathonRegistrationRepository;
    private final IPaymentGateway paymentGateway;
    private final HackathonMapper hackathonMapper;
    private final PrizeDisbursementMapper prizeDisbursementMapper;
    private final CurrentDateProvider currentDateProvider;

    public OrganizerServiceImpl(
            HackathonRepository hackathonRepository,
            OrganizerRepository organizerRepository,
            JudgeRepository judgeRepository,
            MentorRepository mentorRepository,
            PrizeDisbursementRepository prizeDisbursementRepository,
            HackathonRegistrationRepository hackathonRegistrationRepository,
            IPaymentGateway paymentGateway,
            HackathonMapper hackathonMapper,
            PrizeDisbursementMapper prizeDisbursementMapper,
            CurrentDateProvider currentDateProvider) {
        this.hackathonRepository = hackathonRepository;
        this.organizerRepository = organizerRepository;
        this.judgeRepository = judgeRepository;
        this.mentorRepository = mentorRepository;
        this.prizeDisbursementRepository = prizeDisbursementRepository;
        this.hackathonRegistrationRepository = hackathonRegistrationRepository;
        this.paymentGateway = paymentGateway;
        this.hackathonMapper = hackathonMapper;
        this.prizeDisbursementMapper = prizeDisbursementMapper;
        this.currentDateProvider = currentDateProvider;
    }

    @Override
    @Transactional
    public Hackathon createHackathon(Hackathon hackathon, Long organizerId,
                                     Long judgeId, List<Long> mentorIds) {
        if (hackathon == null) {
            throw new IllegalArgumentException("L'hackathon non può essere null");
        }
        if (mentorIds == null || mentorIds.isEmpty()) {
            throw new IllegalArgumentException("Almeno un mentore è obbligatorio");
        }
        validateSchedule(hackathon, currentDateProvider.today());

        Organizer organizer = organizerRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("Organizzatore", organizerId));

        Judge judge = judgeRepository.findById(judgeId)
                .orElseThrow(() -> new ResourceNotFoundException("Giudice", judgeId));

        List<Long> distinctMentorIds = mentorIds.stream().distinct().toList();
        List<Mentor> mentors = mentorRepository.findAllById(distinctMentorIds);
        if (mentors.size() != distinctMentorIds.size()) {
            throw new ResourceNotFoundException("Uno o più mentori non trovati");
        }

        hackathon.setOrganizer(organizer);
        hackathon.addJudge(judge);
        for (Mentor mentor : mentors) {
            hackathon.addMentor(mentor);
        }

        return hackathonRepository.save(hackathon);
    }

    @Override
    @Transactional
    public void addMentorToHackathon(Long hackathonId, Long organizerId, Long mentorId) {
        Hackathon hackathon = findHackathonById(hackathonId);
        ensureOrganizerOwnsHackathon(hackathon, organizerId);
        ensureStaffCanStillBeAssigned(hackathon);

        Mentor mentor = mentorRepository.findById(mentorId)
                .orElseThrow(() -> new ResourceNotFoundException("Mentore", mentorId));

        hackathon.addMentor(mentor);
        hackathonRepository.save(hackathon);
    }

    @Override
    @Transactional
    public void proclaimWinner(Long hackathonId, Long organizerId, Long teamId) {
        Hackathon hackathon = findHackathonById(hackathonId);
        ensureOrganizerOwnsHackathon(hackathon, organizerId);

        HackathonRegistration registration = hackathonRegistrationRepository
                .findByHackathonIdAndTeamId(hackathonId, teamId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Il team " + teamId + " non è iscritto all'hackathon " + hackathonId));

        if (registration.isDisqualified()) {
            throw new ForbiddenOperationException(
                    "Il team " + teamId
                            + " è squalificato e non può essere proclamato vincitore");
        }

        hackathon.concludeWith(registration.getTeam());
        hackathonRepository.save(hackathon);
    }

    @Override
    public List<HackathonResponseDTO> getHackathonsByOrganizer(Long organizerId) {
        Organizer organizer = organizerRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("Organizzatore", organizerId));
        return organizer.getOrganizedHackathons().stream()
                .map(h -> hackathonMapper.toResponse(h, isAlreadyDisbursed(h.getId())))
                .toList();
    }

    @Override
    @Transactional
    public PrizeDisbursementResponseDTO disbursePrize(Long hackathonId, Long organizerId) {
        Hackathon hackathon = findHackathonById(hackathonId);
        ensureOrganizerOwnsHackathon(hackathon, organizerId);
        ensureHackathonConcluded(hackathon);

        Team winningTeam = hackathon.getWinningTeam();
        if (winningTeam == null) {
            // Invariante difensiva: CONCLUDED implica winningTeam non nullo
            // (concludeWith lo impone). Difesa contro stati incoerenti dovuti a
            // modifiche manuali al DB.
            throw new IllegalStateException(
                    "Hackathon " + hackathonId + " concluso senza team vincitore");
        }

        Optional<PrizeDisbursement> existing = prizeDisbursementRepository.findByHackathonId(hackathonId);
        if (existing.isPresent()
                && existing.get().getStatus() == PrizeDisbursementStatus.SUCCESS) {
            throw new PrizeAlreadyDisbursedException(hackathonId);
        }

        PaymentResult result = paymentGateway.payPrize(
                hackathon, winningTeam, hackathon.getPrizeMoney());

        PrizeDisbursement disbursement = existing.orElseGet(PrizeDisbursement::new);
        disbursement.setHackathon(hackathon);
        disbursement.setWinningTeam(winningTeam);
        disbursement.setAmount(hackathon.getPrizeMoney());
        disbursement.setDisbursedAt(LocalDateTime.now());

        if (result.success()) {
            disbursement.setStatus(PrizeDisbursementStatus.SUCCESS);
            disbursement.setTransactionReference(result.transactionReference());
            disbursement.setFailureReason(null);
            PrizeDisbursement saved = prizeDisbursementRepository.save(disbursement);
            return prizeDisbursementMapper.toResponse(saved);
        }

        disbursement.setStatus(PrizeDisbursementStatus.FAILED);
        disbursement.setTransactionReference(null);
        disbursement.setFailureReason(result.failureReason());
        prizeDisbursementRepository.save(disbursement);
        throw new PrizeDisbursementFailedException(hackathonId, result.failureReason());
    }

    @Override
    public PrizeDisbursementResponseDTO getPrizeDisbursement(Long hackathonId, Long organizerId) {
        Hackathon hackathon = findHackathonById(hackathonId);
        ensureOrganizerOwnsHackathon(hackathon, organizerId);
        PrizeDisbursement disbursement = prizeDisbursementRepository
                .findByHackathonId(hackathonId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "PrizeDisbursement", hackathonId));
        return prizeDisbursementMapper.toResponse(disbursement);
    }


    @Override
    @Transactional
    public void removeMentorFromHackathon(Long hackathonId, Long organizerId, Long mentorId) {
        Hackathon hackathon = findHackathonById(hackathonId);
        ensureOrganizerOwnsHackathon(hackathon, organizerId);
        ensureStaffCanStillBeAssigned(hackathon);

        Mentor mentor = mentorRepository.findById(mentorId)
                .orElseThrow(() -> new ResourceNotFoundException("Mentore", mentorId));
        ensureMentorRemovalKeepsMinimum(hackathon);

        hackathon.removeMentor(mentor);
        hackathonRepository.save(hackathon);
    }

    @Override
    @Transactional
    public void addJudgeToHackathon(Long hackathonId, Long organizerId, Long judgeId) {
        Hackathon hackathon = findHackathonById(hackathonId);
        ensureOrganizerOwnsHackathon(hackathon, organizerId);
        ensureStaffCanStillBeAssigned(hackathon);

        if (hackathon.getJudge() != null) {
            throw new IllegalStateException("L'hackathon ha già un giudice assegnato");
        }
        Judge judge = judgeRepository.findById(judgeId)
                .orElseThrow(() -> new ResourceNotFoundException("Giudice", judgeId));
        hackathon.addJudge(judge);
        hackathonRepository.save(hackathon);
    }

    @Override
    @Transactional
    public void replaceJudge(Long hackathonId, Long organizerId, Long newJudgeId) {
        if (newJudgeId == null) {
            throw new IllegalArgumentException("L'ID del nuovo giudice non può essere null");
        }
        Hackathon hackathon = findHackathonById(hackathonId);
        ensureOrganizerOwnsHackathon(hackathon, organizerId);
        ensureStaffCanStillBeAssigned(hackathon);

        Judge currentJudge = hackathon.getJudge();
        if (currentJudge != null && Objects.equals(currentJudge.getId(), newJudgeId)) {
            throw new IllegalArgumentException(
                    "Il giudice " + newJudgeId + " è già assegnato all'hackathon " + hackathonId);
        }

        Judge newJudge = judgeRepository.findById(newJudgeId)
                .orElseThrow(() -> new ResourceNotFoundException("Judge", newJudgeId));

        if (currentJudge != null) {
            hackathon.removeJudge();
        }
        hackathon.addJudge(newJudge);
        hackathonRepository.save(hackathon);
    }

    @Override
    @Transactional
    public void cancelHackathon(Long hackathonId, Long organizerId) {
        Hackathon hackathon = findHackathonById(hackathonId);
        ensureOrganizerOwnsHackathon(hackathon, organizerId);

        hackathon.cancel();
        hackathonRepository.save(hackathon);
    }

    @Override
    @Transactional
    public void updateHackathon(
            Long hackathonId, Long organizerId, UpdateHackathonRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("La richiesta di modifica non può essere null");
        }
        Hackathon hackathon = findHackathonById(hackathonId);
        ensureOrganizerOwnsHackathon(hackathon, organizerId);

        hackathon.update(
                request.name(),
                request.rules(),
                request.location(),
                request.prizeMoney(),
                request.maxTeamSize(),
                request.registrationDeadline(),
                request.startDate(),
                request.endDate(),
                currentDateProvider.today());
        hackathonRepository.save(hackathon);
    }


    @Override
    public List<StaffMemberSummaryDTO> getAvailableMentors() {
        return mentorRepository.findAll().stream()
                .map(this::toStaffMemberSummary)
                .toList();
    }

    @Override
    public List<StaffMemberSummaryDTO> getAvailableJudges() {
        return judgeRepository.findAll().stream()
                .map(this::toStaffMemberSummary)
                .toList();
    }

    @Override
    public List<StaffMemberSummaryDTO> getMentorsByHackathon(Long hackathonId, Long organizerId) {
        Hackathon hackathon = findHackathonById(hackathonId);
        ensureOrganizerOwnsHackathon(hackathon, organizerId);
        return hackathon.getMentors().stream()
                .map(this::toStaffMemberSummary)
                .toList();
    }

    @Override
    public List<Submission> getHackathonSubmissions(Long hackathonId, Long organizerId) {
        Hackathon hackathon = findHackathonById(hackathonId);
        ensureOrganizerOwnsHackathon(hackathon, organizerId);
        // L'organizzatore è l'admin dell'evento: vede tutte le sottomissioni,
        // incluse quelle dei team squalificati (ADR 0005 nasconde gli squalificati
        // solo dalla consultazione pubblica e dalla classifica, non all'organizzatore).
        return hackathon.getRegistrations().stream()
                .map(HackathonRegistration::getSubmission)
                .filter(Objects::nonNull)
                .toList();
    }

    private boolean isAlreadyDisbursed(Long hackathonId) {
        return prizeDisbursementRepository.findByHackathonId(hackathonId)
                .map(d -> d.getStatus() == PrizeDisbursementStatus.SUCCESS)
                .orElse(false);
    }

    private Hackathon findHackathonById(Long hackathonId) {
        if (hackathonId == null) {
            throw new IllegalArgumentException("L'ID non può essere null");
        }
        Hackathon hackathon = hackathonRepository.findById(hackathonId)
                .orElseThrow(() -> new ResourceNotFoundException("Hackathon", hackathonId));
        hackathon.updateStatus(currentDateProvider.today());
        return hackathon;
    }

    private void validateSchedule(Hackathon hackathon, LocalDate currentDate) {
        LocalDate registrationDeadline = hackathon.getRegistrationDeadline();
        LocalDate startDate = hackathon.getStartDate();
        LocalDate endDate = hackathon.getEndDate();
        if (registrationDeadline == null || startDate == null || endDate == null) {
            throw new IllegalArgumentException("Le date non possono essere null");
        }
        if (hackathon.getPrizeMoney() == null || hackathon.getPrizeMoney().signum() < 0) {
            throw new IllegalArgumentException("Il premio in denaro non può essere negativo");
        }
        if (hackathon.getMaxTeamSize() <= 0) {
            throw new IllegalArgumentException("La dimensione massima del team deve essere positiva");
        }
        if (registrationDeadline.isBefore(currentDate)) {
            throw new IllegalArgumentException("La scadenza iscrizioni non può essere nel passato");
        }
        if (startDate.isBefore(registrationDeadline)) {
            throw new IllegalArgumentException(
                    "La data di inizio deve essere successiva alla scadenza iscrizioni");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "La data di fine deve essere successiva alla data di inizio");
        }
    }

    private void ensureStaffCanStillBeAssigned(Hackathon hackathon) {
        HackathonStatus status = hackathon.getStatus();
        if (status != HackathonStatus.REGISTRATION
                && status != HackathonStatus.RUNNING) {
            throw new InvalidHackathonStateException(
                    hackathon.getId(),
                    status,
                    HackathonStatus.REGISTRATION,
                    HackathonStatus.RUNNING);
        }
    }

    private void ensureMentorRemovalKeepsMinimum(Hackathon hackathon) {
        if (hackathon.getMentors().size() <= 1) {
            throw new IllegalStateException("L'hackathon deve avere almeno un mentore");
        }
    }

    private void ensureHackathonConcluded(Hackathon hackathon) {
        if (hackathon.getStatus() != HackathonStatus.CONCLUDED) {
            throw new InvalidHackathonStateException(
                    hackathon.getId(),
                    hackathon.getStatus(),
                    HackathonStatus.CONCLUDED);
        }
    }

    private void ensureOrganizerOwnsHackathon(Hackathon hackathon, Long organizerId) {
        if (organizerId == null) {
            throw new IllegalArgumentException("L'ID dell'organizzatore non può essere null");
        }
        Organizer organizer = hackathon.getOrganizer();
        if (organizer == null || !Objects.equals(organizer.getId(), organizerId)) {
            throw new ForbiddenOperationException(
                    "L'organizzatore " + organizerId
                            + " non è autorizzato per l'hackathon " + hackathon.getId());
        }
    }

    private StaffMemberSummaryDTO toStaffMemberSummary(User staffMember) {
        return new StaffMemberSummaryDTO(
                staffMember.getId(),
                staffMember.getName(),
                staffMember.getEmail());
    }

    @Override
    @Transactional
    public void disqualifyTeam(Long hackathonId, Long organizerId, Long teamId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("La motivazione della squalifica è obbligatoria");
        }
        Hackathon hackathon = findHackathonById(hackathonId);
        ensureOrganizerOwnsHackathon(hackathon, organizerId);
        ensureHackathonAllowsDisqualification(hackathon);

        HackathonRegistration registration = hackathonRegistrationRepository
                .findByHackathonIdAndTeamId(hackathonId, teamId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Il team " + teamId + " non è iscritto all'hackathon " + hackathonId));

        if (registration.isDisqualified()) {
            throw new ForbiddenOperationException(
                    "Il team " + teamId + " è già squalificato dall'hackathon " + hackathonId);
        }

        registration.setStatus(RegistrationStatus.DISQUALIFIED);
        registration.setDisqualificationReason(reason.strip());
        registration.setDisqualifiedAt(LocalDateTime.now());
        hackathonRegistrationRepository.save(registration);
    }

    private void ensureHackathonAllowsDisqualification(Hackathon hackathon) {
        HackathonStatus status = hackathon.getStatus();
        if (status != HackathonStatus.RUNNING && status != HackathonStatus.EVALUATION) {
            throw new InvalidHackathonStateException(
                    hackathon.getId(), status,
                    HackathonStatus.RUNNING, HackathonStatus.EVALUATION);
        }
    }
}
