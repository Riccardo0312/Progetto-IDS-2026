package it.unicam.cs.ids.hackhub.exception;

import it.unicam.cs.ids.hackhub.model.InvitationStatus;

public class InvalidInvitationStateException extends RuntimeException {

    private final Long invitationId;
    private final InvitationStatus currentState;
    private final InvitationStatus expectedState;

    public InvalidInvitationStateException(Long invitationId, InvitationStatus currentState, InvitationStatus expectedState) {
        super("Invito " + invitationId + " non valido: stato corrente = " + currentState + ", atteso = " + expectedState);
        this.invitationId = invitationId;
        this.currentState = currentState;
        this.expectedState = expectedState;
    }

    public Long getInvitationId() {
        return invitationId;
    }

    public InvitationStatus getCurrentState() {
        return currentState;
    }

    public InvitationStatus getExpectedState() {
        return expectedState;
    }
}
