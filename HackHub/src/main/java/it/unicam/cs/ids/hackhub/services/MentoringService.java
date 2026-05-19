package it.unicam.cs.ids.hackhub.services;

import it.unicam.cs.ids.hackhub.model.HackathonRegistration;
import it.unicam.cs.ids.hackhub.model.SupportRequest;
import it.unicam.cs.ids.hackhub.model.repository.HackathonRegistrationRepository;
import it.unicam.cs.ids.hackhub.model.repository.SupportRequestRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MentoringService {

    @Autowired
    private SupportRequestRepository supportRequestRepository;
    @Autowired
    private HackathonRegistrationRepository registrationRepository;

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
