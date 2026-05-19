package it.unicam.cs.ids.hackhub.model.state;

import it.unicam.cs.ids.hackhub.model.InvitationStatus;

public final class InvitationStateFactory {
    private static final InvitationState PENDING = new PendingState();
    private static final InvitationState ACCEPTED = new AcceptedState();
    private static final InvitationState REJECTED = new RejectedState();

    public static InvitationState fromStatus(InvitationStatus status) {
        return switch(status) {
            case PENDING -> PENDING;
            case ACCEPTED -> ACCEPTED;
            case REJECTED -> REJECTED;
        };
    }
}