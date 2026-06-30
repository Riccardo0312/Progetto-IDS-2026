package it.unicam.cs.ids.hackhub.service.interfaces;

import it.unicam.cs.ids.hackhub.dto.hackathon.TeamRegistrationResponseDTO;

/**
 * Servizio per il caso d'uso "Iscrizione team a hackathon".
 *
 * <p>La sola consultazione pubblica delle iscrizioni e la lista hackathon sono
 * coperte da {@code IGuestService}; qui resta unicamente l'azione di scrittura.
 */
public interface IHackathonRegistrationService {

    /**
     * Iscrive il team {@code teamId} all'hackathon {@code hackathonId}.
     *
     * @throws it.unicam.cs.ids.hackhub.exception.ResourceNotFoundException
     *         se hackathon o team non esistono
     * @throws it.unicam.cs.ids.hackhub.exception.InvalidHackathonStateException
     *         se l'hackathon non accetta nuove iscrizioni
     * @throws IllegalStateException se il team è già iscritto
     */
    TeamRegistrationResponseDTO registerTeam(Long hackathonId, Long teamId);
}
