package it.unicam.cs.ids.hackhub.service.interfaces;

import it.unicam.cs.ids.hackhub.model.Invitation;
import java.util.List;

public interface IInvitationService {
    Invitation sendInvitation(Long teamId, String recipientEmail, String senderEmail);
    Invitation acceptInvitation(Long invitationId, String userEmail);
    Invitation rejectInvitation(Long invitationId, String userEmail);
    List<Invitation> getPendingInvitations(String userEmail);
}
