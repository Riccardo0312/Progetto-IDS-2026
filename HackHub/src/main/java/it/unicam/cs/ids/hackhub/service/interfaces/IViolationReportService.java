package it.unicam.cs.ids.hackhub.service.interfaces;

import it.unicam.cs.ids.hackhub.dto.support.ViolationReportDTO;
import java.util.List;

public interface IViolationReportService {

    /**
     * Restituisce tutte le segnalazioni relative a un hackathon
     * per lo staff assegnato a quello specifico hackathon.
     *
     * @param hackathonId ID dell'hackathon
     * @param userEmail email dell'utente autenticato
     * @return lista di segnalazioni
     */
    List<ViolationReportDTO> viewReportsForHackathon(Long hackathonId, String userEmail);
}