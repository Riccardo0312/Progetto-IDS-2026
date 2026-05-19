package it.unicam.cs.ids.hackhub.config;

import it.unicam.cs.ids.hackhub.service.interfaces.CalendarGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CalendarGatewayConfiguration {

	@Bean
	public CalendarGateway developmentCalendarGateway() {
		return (supportRequest, mentor) ->
				"https://calendar.hackhub.local/bookings/support-"
						+ supportRequest.getId()
						+ "/mentor-"
						+ mentor.getId();
	}

}
