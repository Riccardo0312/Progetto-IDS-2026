package it.unicam.cs.ids.hackhub.service.interfaces;

import it.unicam.cs.ids.hackhub.model.Hackathon;
import it.unicam.cs.ids.hackhub.model.PaymentResult;
import it.unicam.cs.ids.hackhub.model.Team;
import java.math.BigDecimal;

/**
 * Adapter verso il Sistema di Pagamento esterno.
 *
 * <p>L'esito dell'operazione viene restituito come {@link PaymentResult} così
 * che il dominio possa registrare l'erogazione (riferimento di transazione o
 * motivo del fallimento) senza dipendere dai dettagli del provider.
 */
public interface IPaymentGateway {

	PaymentResult payPrize(Hackathon hackathon, Team winningTeam, BigDecimal amount);
}
