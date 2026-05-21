package it.unicam.cs.ids.hackhub.dto;

/**
 * Vista compatta di un {@code Team} per le risposte API che non devono esporre
 * l'entità JPA completa.
 */
public record TeamSummaryDTO(Long id, String name) {}
