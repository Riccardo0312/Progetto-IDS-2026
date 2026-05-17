package it.unicam.cs.ids.hackhub.service.impl;

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

import it.unicam.cs.ids.hackhub.service.interfaces.OrganizerService;
import it.unicam.cs.ids.hackhub.service.interfaces.PaymentGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrganizerServiceImpl implements OrganizerService {

    private final HackathonRepository hackathonRepository;
    private final OrganizerRepository organizerRepository;
    private final JudgeRepository judgeRepository;
    private final MentorRepository mentorRepository;
    private final PaymentGateway paymentGateway;

    public OrganizerServiceImpl(HackathonRepository hackathonRepository,
                                OrganizerRepository organizerRepository,
                                JudgeRepository judgeRepository,
                                MentorRepository mentorRepository,
                                PaymentGateway paymentGateway) {
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

        List<Mentor> mentors = mentorRepository.findAllById(mentorIds);
        if (mentors.size() != mentorIds.size()) {
            throw new IllegalArgumentException("Uno o più mentori non trovati");
        }

        hackathon.setOrganizer(organizer);
        hackathon.setStatus(HackathonStatus.REGISTRATION);
        hackathon.addJudge(judge);
        for (int i = 0; i < mentors.size(); i++) {
            hackathon.addMentor(mentors.get(i));
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

        hackathon.addMentor(mentor);
        hackathonRepository.save(hackathon);
    }

    @Override
    @Transactional
    public void proclameWinner(Long hackathonId, Team winningTeam) {
        if (winningTeam == null) {
            throw new IllegalArgumentException("Il team vincitore non può essere null");
        }

        Hackathon hackathon = findHackathonById(hackathonId);

        if (hackathon.getStatus() != HackathonStatus.EVALUATION) {
            throw new IllegalStateException(
                    "Il vincitore può essere proclamato solo in fase di valutazione");
        }

        if (!hackathon.allEvaluated()) {
            throw new IllegalStateException(
                    "Ci sono ancora sottomissioni non valutate");
        }

        hackathon.setWinningTeam(winningTeam);
        hackathon.setStatus(HackathonStatus.CONCLUDED);
        hackathonRepository.save(hackathon);

        paymentGateway.payPrize(hackathon, winningTeam, hackathon.getPrizeMoney());
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
        return hackathonRepository.findById(hackathonId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Hackathon non trovato con ID: " + hackathonId));
    }
}
