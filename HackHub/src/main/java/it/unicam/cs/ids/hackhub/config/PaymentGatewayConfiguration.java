package it.unicam.cs.ids.hackhub.config;

import it.unicam.cs.ids.hackhub.model.PaymentResult;
import it.unicam.cs.ids.hackhub.service.interfaces.IPaymentGateway;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Stub di sviluppo per {@link IPaymentGateway}: simula un Sistema di Pagamento
 * esterno che eroga sempre con esito positivo, restituendo un riferimento di
 * transazione fittizio.
 */
@Configuration
public class PaymentGatewayConfiguration {

	@Bean
	public IPaymentGateway developmentPaymentGateway() {
		return (hackathon, winningTeam, amount) ->
				PaymentResult.success("FAKE-TX-" + UUID.randomUUID());
	}
}
