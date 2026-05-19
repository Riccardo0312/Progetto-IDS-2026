package it.unicam.cs.ids.hackhub.model.state;

import it.unicam.cs.ids.hackhub.model.InvitationStatus;

public final class RejectedState implements InvitationState {
    @Override
    public InvitationStatus getStatus() { return InvitationStatus.REJECTED; }
}