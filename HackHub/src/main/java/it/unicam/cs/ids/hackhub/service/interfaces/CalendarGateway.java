package it.unicam.cs.ids.hackhub.service.interfaces;

import it.unicam.cs.ids.hackhub.model.Mentor;
import it.unicam.cs.ids.hackhub.model.SupportRequest;

public interface CalendarGateway {

	String createBookingLink(SupportRequest supportRequest, Mentor mentor);

}
