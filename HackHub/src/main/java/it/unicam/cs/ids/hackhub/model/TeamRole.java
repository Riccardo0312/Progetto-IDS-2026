package it.unicam.cs.ids.hackhub.model;

/**
 * Ruolo di un {@link TeamMember} all'interno di un {@link Team}.
 *
 * <p>Modella la specializzazione di attore "Leader Team" del diagramma dei
 * casi d'uso senza introdurre una sottoclasse di dominio: la differenza tra
 * leader e membro è solo autorizzativa, non strutturale.
 */
public enum TeamRole {
    /** Unico per team. Può invitare, rimuovere membri, eliminare il team. */
    LEADER,
    /** Membro standard. Permessi base. */
    MEMBER
}
