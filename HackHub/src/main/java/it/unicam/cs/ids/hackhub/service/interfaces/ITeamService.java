package it.unicam.cs.ids.hackhub.service.interfaces;

import it.unicam.cs.ids.hackhub.model.Team;

public interface ITeamService {

    Team createTeam(String name, String creatorEmail);

    /**
     * Lascia il team in modo atomico.
     *
     * <p>Se {@code userEmail} è il leader: {@code successorEmail} obbligatorio;
     * il successore viene promosso a LEADER e il leader uscente viene rimosso.
     * Se {@code userEmail} è un membro normale: {@code successorEmail} deve
     * essere null (altrimenti errore); il membro viene rimosso.
     *
     * @throws IllegalArgumentException  se i parametri sono incoerenti col ruolo
     * @throws IllegalStateException     se il leader è l'unico membro del team
     * @throws ForbiddenOperationException se l'utente non appartiene al team
     */
    void leaveTeam(Long teamId, String userEmail, String successorEmail);
}
