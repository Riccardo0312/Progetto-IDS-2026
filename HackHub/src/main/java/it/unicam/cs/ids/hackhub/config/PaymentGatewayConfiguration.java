package it.unicam.cs.ids.hackhub.config;

import it.unicam.cs.ids.hackhub.service.interfaces.PaymentGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentGatewayConfiguration {

	@Bean
	public PaymentGateway developmentPaymentGateway() {
		return (hackathon, winningTeam, amount) -> {
		};
	}

}
