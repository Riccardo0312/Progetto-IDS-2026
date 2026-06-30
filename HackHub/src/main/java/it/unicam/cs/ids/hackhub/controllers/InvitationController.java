package it.unicam.cs.ids.hackhub.controllers;

import it.unicam.cs.ids.hackhub.dto.invitation.CreateInvitationRequestDTO;
import it.unicam.cs.ids.hackhub.dto.invitation.InvitationSummaryDTO;
import it.unicam.cs.ids.hackhub.model.Invitation;
import it.unicam.cs.ids.hackhub.service.interfaces.IInvitationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invitations")
public class InvitationController {

    private final IInvitationService invitationService;

    public InvitationController(IInvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public List<InvitationSummaryDTO> getPendingInvitations(Authentication authentication) {
        return invitationService.getPendingInvitations(authentication.getName()).stream()
                .map(this::toSummary)
                .toList();
    }

    /**
     * Invia un invito a un utente per unirsi a un team. Solo il leader del team
     * indicato nel body può eseguire l'operazione; il mittente è il principal JWT.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@hackHubAuthorizationService.isTeamLeader(#request.teamId(), authentication.name)")
    public InvitationSummaryDTO sendInvitation(
            @Valid @RequestBody CreateInvitationRequestDTO request,
            Authentication authentication) {
        Invitation invitation = invitationService.sendInvitation(
                request.teamId(),
                request.recipientEmail(),
                authentication.getName());
        return toSummary(invitation);
    }

    @PostMapping("/{invitationId}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER')")
    public void acceptInvitation(@PathVariable Long invitationId, Authentication authentication) {
        invitationService.acceptInvitation(invitationId, authentication.getName());
    }

    @PostMapping("/{invitationId}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('USER')")
    public void rejectInvitation(@PathVariable Long invitationId, Authentication authentication) {
        invitationService.rejectInvitation(invitationId, authentication.getName());
    }

    private InvitationSummaryDTO toSummary(Invitation invitation) {
        String leaderName = invitation.getTeam().findLeader()
                .map(m -> m.getUser().getName())
                .orElse(null);
        return new InvitationSummaryDTO(
                invitation.getId(),
                invitation.getTeam().getId(),
                invitation.getTeam().getName(),
                leaderName);
    }
}
