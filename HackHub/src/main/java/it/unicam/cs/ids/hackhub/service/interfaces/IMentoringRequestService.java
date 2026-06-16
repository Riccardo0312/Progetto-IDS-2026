package it.unicam.cs.ids.hackhub.service.interfaces;

import it.unicam.cs.ids.hackhub.model.SupportRequest;
import java.util.List;

public interface IMentoringRequestService {
    List<SupportRequest> getTeamSupportRequests(Long teamId, String userEmail);

    SupportRequest getTeamSupportRequest(Long teamId, Long supportRequestId, String userEmail);

    SupportRequest createSupportRequest(Long registrationId, String userEmail, String description);
}
