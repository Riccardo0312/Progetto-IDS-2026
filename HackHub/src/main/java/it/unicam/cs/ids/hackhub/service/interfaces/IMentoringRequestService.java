package it.unicam.cs.ids.hackhub.service.interfaces;

import it.unicam.cs.ids.hackhub.model.SupportRequest;

public interface IMentoringRequestService {
    SupportRequest createSupportRequest(Long registrationId, String message);
}
