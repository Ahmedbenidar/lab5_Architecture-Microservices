package mliaedu.tp5.service;

/**
 * Ligne d'un import de catalogue : le service ne manipule pas d'entité
 * détachée, il reçoit des données brutes et construit les entités lui-même.
 */
public record LigneProduit(String reference, String name, double price) {
}
