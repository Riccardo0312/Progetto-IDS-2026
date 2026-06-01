package it.unicam.cs.ids.hackhub.service.interfaces;

import it.unicam.cs.ids.hackhub.dto.team.TeamDetailsDTO;
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
     */
    void leaveTeam(Long teamId, String userEmail, String successorEmail);

    /**
     * Elimina il team. Solo il leader può farlo.
     *
     * <p>Consentito solo se tutte le registrazioni del team sono per hackathon
     * in stato {@code REGISTRATION}. Cascade: rimuove TeamMember e Invitation.
     */
    void deleteTeam(Long teamId, String leaderEmail);

    /**
     * Restituisce i dettagli del team richiesto.
     *
     * <p>L'utente identificato da {@code userEmail} deve esistere e appartenere
     * al team {@code teamId}; in caso contrario l'operazione viene rifiutata.
     */
    TeamDetailsDTO viewTeam(Long teamId, String userEmail);

}
