package it.unicam.cs.ids.hackhub.service.impl;

import it.unicam.cs.ids.hackhub.exception.InvalidHackathonStateException;
import it.unicam.cs.ids.hackhub.model.Hackathon;
import it.unicam.cs.ids.hackhub.model.HackathonStatus;
import it.unicam.cs.ids.hackhub.model.Judge;
import it.unicam.cs.ids.hackhub.model.Mentor;
import it.unicam.cs.ids.hackhub.model.Organizer;
import it.unicam.cs.ids.hackhub.model.Team;
import it.unicam.cs.ids.hackhub.model.repository.HackathonRepository;
import it.unicam.cs.ids.hackhub.model.repository.JudgeRepository;
import it.unicam.cs.ids.hackhub.model.repository.MentorRepository;
import it.unicam.cs.ids.hackhub.model.repository.OrganizerRepository;
import java.util.List;

import it.unicam.cs.ids.hackhub.service.interfaces.IOrganizerService;
import it.unicam.cs.ids.hackhub.service.interfaces.IPaymentGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@Transactional(readOnly = true)
public class OrganizerServiceImpl implements IOrganizerService {

    private final HackathonRepository hackathonRepository;
    private final OrganizerRepository organizerRepository;
    private final JudgeRepository judgeRepository;
    private final MentorRepository mentorRepository;
    private final IPaymentGateway paymentGateway;

    public OrganizerServiceImpl(HackathonRepository hackathonRepository,
                                OrganizerRepository organizerRepository,
                                JudgeRepository judgeRepository,
                                MentorRepository mentorRepository,
                                IPaymentGateway paymentGateway) {
        this.hackathonRepository = hackathonRepository;
        this.organizerRepository = organizerRepository;
        this.judgeRepository = judgeRepository;
        this.mentorRepository = mentorRepository;
        this.paymentGateway = paymentGateway;
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

        Organizer organizer = organizerRepository.findById(organizerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Organizzatore non trovato con ID: " + organizerId));

        Judge judge = judgeRepository.findById(judgeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Giudice non trovato con ID: " + judgeId));

        List<Long> distinctMentorIds = mentorIds.stream().distinct().toList();
        List<Mentor> mentors = mentorRepository.findAllById(distinctMentorIds);
        if (mentors.size() != distinctMentorIds.size()) {
            throw new IllegalArgumentException("Uno o più mentori non trovati");
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
    public void addMentorToHackathon(Long hackathonId, Long mentorId) {
        Hackathon hackathon = findHackathonById(hackathonId);
        Mentor mentor = mentorRepository.findById(mentorId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Mentore non trovato con ID: " + mentorId));

        ensureMentorCanStillBeAssigned(hackathon);
        hackathon.addMentor(mentor);
        hackathonRepository.save(hackathon);
    }

    @Override
    @Transactional
    public void proclaimWinner(Long hackathonId, Team winningTeam) {
        Hackathon hackathon = findHackathonById(hackathonId);

        // Transizione esplicita gestita dal modello: applica le guardie di stato,
        // verifica le valutazioni e imposta winningTeam + status = CONCLUDED.
        hackathon.concludeWith(winningTeam);
        hackathonRepository.save(hackathon);

        payPrizeAfterCommit(hackathon, winningTeam);
    }

    @Override
    public List<Hackathon> getHackathonsByOrganizer(Long organizerId) {
        Organizer organizer = organizerRepository.findById(organizerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Organizzatore non trovato con ID: " + organizerId));
        return organizer.getOrganizedHackathons();
    }

    private Hackathon findHackathonById(Long hackathonId) {
        if (hackathonId == null) {
            throw new IllegalArgumentException("L'ID non può essere null");
        }
        Hackathon hackathon = hackathonRepository.findById(hackathonId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Hackathon non trovato con ID: " + hackathonId));
        // Allinea lo status persistito al tempo reale: la proclamazione del vincitore
        // deve poter avvenire appena l'hackathon entra in EVALUATION, anche se nessuno
        // ha ancora ricaricato l'entita.
        hackathon.updateStatus();
        return hackathon;
    }

    private void ensureMentorCanStillBeAssigned(Hackathon hackathon) {
        if (hackathon.getStatus() == HackathonStatus.EVALUATION
                || hackathon.getStatus() == HackathonStatus.CONCLUDED) {
            throw new InvalidHackathonStateException(
                    hackathon.getId(),
                    hackathon.getStatus(),
                    HackathonStatus.REGISTRATION,
                    HackathonStatus.RUNNING);
        }
    }

    private void payPrizeAfterCommit(Hackathon hackathon, Team winningTeam) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            paymentGateway.payPrize(hackathon, winningTeam, hackathon.getPrizeMoney());
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        paymentGateway.payPrize(hackathon, winningTeam, hackathon.getPrizeMoney());
                    }
                });
    }
}
