package it.unicam.cs.ids.hackhub.service.impl;

import it.unicam.cs.ids.hackhub.model.HackathonRegistration;
import it.unicam.cs.ids.hackhub.model.SupportRequest;
import it.unicam.cs.ids.hackhub.model.repository.HackathonRegistrationRepository;
import it.unicam.cs.ids.hackhub.model.repository.SupportRequestRepository;
import it.unicam.cs.ids.hackhub.service.interfaces.IMentoringRequestService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MentoringService implements IMentoringRequestService {

    private final SupportRequestRepository supportRequestRepository;
    private final HackathonRegistrationRepository registrationRepository;

    public MentoringService(SupportRequestRepository supportRequestRepository,
                            HackathonRegistrationRepository registrationRepository) {
        this.supportRequestRepository = supportRequestRepository;
        this.registrationRepository = registrationRepository;
    }

    @Override
    @Transactional
    public SupportRequest createSupportRequest(Long registrationId, String message) {
        HackathonRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Registrazione non trovata"));

        SupportRequest request = new SupportRequest();
        request.setTeam(registration.getTeam());
        request.setHackathon(registration.getHackathon());
        request.setMessage(message);
        request.setRequestDate(LocalDateTime.now());

        return supportRequestRepository.save(request);
    }
}
