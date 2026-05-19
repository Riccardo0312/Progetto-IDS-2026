package it.unicam.cs.ids.hackhub.model.state;

import it.unicam.cs.ids.hackhub.exception.InvalidInvitationStateException;
import it.unicam.cs.ids.hackhub.model.InvitationStatus;

public sealed interface InvitationState
        permits PendingState, AcceptedState, RejectedState {

    InvitationStatus getStatus();

    default void ensureCanAccept(Long invitationId) {
        throw new InvalidInvitationStateException(invitationId, getStatus(), InvitationStatus.PENDING);
    }

    default void ensureCanReject(Long invitationId) {
        throw new InvalidInvitationStateException(invitationId, getStatus(), InvitationStatus.PENDING);
    }
}