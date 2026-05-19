package it.unicam.cs.ids.hackhub.model.state.invitation;

import it.unicam.cs.ids.hackhub.model.InvitationStatus;

public final class PendingState implements InvitationState {
    @Override
    public InvitationStatus getStatus() { return InvitationStatus.PENDING; }
    @Override
    public void ensureCanAccept(Long invitationId) { /* consentito */ }
    @Override
    public void ensureCanReject(Long invitationId) { /* consentito */ }
}

