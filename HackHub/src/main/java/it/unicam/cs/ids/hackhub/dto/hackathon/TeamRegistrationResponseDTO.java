package it.unicam.cs.ids.hackhub.dto.hackathon;

import it.unicam.cs.ids.hackhub.model.RegistrationStatus;
import java.time.LocalDateTime;

/**
 * Esito dell'iscrizione di un team a un hackathon.
 */
public record TeamRegistrationResponseDTO(
        Long registrationId,
        Long hackathonId,
        Long teamId,
        RegistrationStatus status,
        LocalDateTime registrationDate) {
}
