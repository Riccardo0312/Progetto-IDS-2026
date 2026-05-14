package it.unicam.cs.ids.hackhub.service.interfaces;

import it.unicam.cs.ids.hackhub.model.Hackathon;
import it.unicam.cs.ids.hackhub.model.Team;
import java.math.BigDecimal;

public interface PaymentGateway {

	void payPrize(Hackathon hackathon, Team winningTeam, BigDecimal amount);

}
