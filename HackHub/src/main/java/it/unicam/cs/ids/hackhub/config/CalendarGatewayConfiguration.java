package it.unicam.cs.ids.hackhub.config;

import it.unicam.cs.ids.hackhub.service.interfaces.ICalendarGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CalendarGatewayConfiguration {

	@Bean
	public ICalendarGateway developmentCalendarGateway() {
		return (supportRequest, mentor) ->
				"https://calendar.hackhub.local/bookings/support-"
						+ supportRequest.getId()
						+ "/mentor-"
						+ mentor.getId();
	}

}
