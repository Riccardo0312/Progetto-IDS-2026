package it.unicam.cs.ids.hackhub.service.interfaces;

import it.unicam.cs.ids.hackhub.model.User;
import it.unicam.cs.ids.hackhub.model.ViolationReport;
import java.util.List;

public interface IViolationReportService {

    /**
     * Restituisce tutte le segnalazioni relative a un hackathon
     * per un utente autenticato (staff o membro del team).
     *
     * @param hackathonId ID dell'hackathon
     * @param user utente autenticato
     * @return lista di segnalazioni
     */
    List<ViolationReport> viewReportsForHackathon(Long hackathonId, User user);
}
