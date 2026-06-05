package it.unicam.cs.ids.hackhub.model;

/**
 * Ruolo globale di piattaforma di un {@link User}.
 *
 * <p>Mappato su authority Spring Security come {@code ROLE_<name>} (es.
 * {@code ORGANIZER} -> {@code ROLE_ORGANIZER}). Distinto da {@link TeamRole},
 * che è il ruolo contestuale all'interno di un team e non un'authority globale.
 */
public enum UserRole {
    /** Partecipante registrato generico: crea/entra in team, invia submission. */
    USER,
    /** Organizza hackathon e assegna staff. */
    ORGANIZER,
    /** Opera sugli hackathon a cui è assegnato. */
    MENTOR,
    /** Valuta le submission degli hackathon a cui è assegnato. */
    JUDGE,
    /** Operazioni amministrative trasversali. */
    ADMIN
}
